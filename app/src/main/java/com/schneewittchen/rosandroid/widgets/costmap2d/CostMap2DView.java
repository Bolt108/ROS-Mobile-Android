package com.schneewittchen.rosandroid.widgets.costmap2d;

import android.content.Context;

import com.google.common.base.Preconditions;
import com.schneewittchen.rosandroid.ui.opengl.visualisation.TextureBitmap;
import com.schneewittchen.rosandroid.ui.opengl.visualisation.Tile;
import com.schneewittchen.rosandroid.ui.opengl.visualisation.VisualizationView;
import com.schneewittchen.rosandroid.ui.views.widgets.SubscriberLayerView;

import org.jboss.netty.buffer.ChannelBuffer;
import org.ros.internal.message.Message;
import org.ros.namespace.GraphName;
import org.ros.rosjava_geometry.Quaternion;
import org.ros.rosjava_geometry.Transform;
import org.ros.rosjava_geometry.Vector3;

import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.opengles.GL10;

import nav_msgs.OccupancyGrid;


/**
 * TODO: Description
 *
 * @author Nico Studt
 * @version 1.0.0
 * @created on 08.03.21
 */
public class CostMap2DView extends SubscriberLayerView {

    public static final String TAG = CostMap2DView.class.getSimpleName();

    private static final int COLOR_OCCUPIED = 0xff111111;
    private static final int COLOR_FREE = 0xddffffff;
    private static final int COLOR_UNKNOWN = 0x88ffffff;
    private static final int COLOR_TRANSPARENT = 0x00000000;

    private List<Tile> tiles;
    private GL10 previousGl;


    public CostMap2DView(Context context) {
        super(context);
        tiles = new ArrayList<>();
    }


    @Override
    public void draw(VisualizationView view, GL10 gl) {
        if (previousGl != gl) {
            for (Tile tile : tiles) {
                tile.clearHandle();
            }
            previousGl = gl;
        }

        for (Tile tile : tiles) {
            tile.draw(view, gl);
        }
    }

    @Override
    public void onNewMessage(Message message) {
        OccupancyGrid grid = (OccupancyGrid) message;

        final float resolution = grid.getInfo().getResolution();
        final int width = grid.getInfo().getWidth();
        final int height = grid.getInfo().getHeight();
        final int numTilesWide = (int) Math.ceil(width / (float) TextureBitmap.STRIDE);
        final int numTilesHigh = (int) Math.ceil(height / (float) TextureBitmap.STRIDE);

        final int numTiles = numTilesWide * numTilesHigh;
        final Transform origin = Transform.fromPoseMessage(grid.getInfo().getOrigin());

        while (tiles.size() < numTiles) {
            tiles.add(new Tile(resolution));
        }

        for (int y = 0; y < numTilesHigh; ++y) {
            for (int x = 0; x < numTilesWide; ++x) {
                final int tileIndex = y * numTilesWide + x;
                tiles.get(tileIndex).setOrigin(origin.multiply(new Transform(new Vector3(
                        x * resolution * width,
                        y * resolution * height,
                        0.),
                        Quaternion.identity())));
                tiles.get(tileIndex).setStride(width);
            }
        }

        int x = 0;
        int y = 0;
        final ChannelBuffer buffer = grid.getData();
        while (buffer.readable()) {
            Preconditions.checkState(y < height);
            final int tileIndex = (y / TextureBitmap.STRIDE) * numTilesWide + x / TextureBitmap.STRIDE;
            final byte pixel = buffer.readByte();
            if (pixel == -1) {
                tiles.get(tileIndex).writeInt(COLOR_UNKNOWN);
            } else if (pixel < 50) {
                tiles.get(tileIndex).writeInt(COLOR_FREE);
            } else {
                tiles.get(tileIndex).writeInt(COLOR_OCCUPIED);
            }

            ++x;
            if (x == width) {
                x = 0;
                ++y;
            }
        }

        for (Tile tile : tiles) {
            tile.update();
        }

        frame = GraphName.of(grid.getHeader().getFrameId());
    }

}