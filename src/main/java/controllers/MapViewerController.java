package controllers;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.OSMTileFactoryInfo;
import org.jxmapviewer.viewer.DefaultTileFactory;
import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.viewer.TileFactoryInfo;
import org.jxmapviewer.viewer.DefaultWaypoint;
import org.jxmapviewer.viewer.Waypoint;
import org.jxmapviewer.viewer.WaypointPainter;
import org.jxmapviewer.painter.CompoundPainter;
import org.jxmapviewer.painter.Painter;
import org.jxmapviewer.input.CenterMapListener;
import org.jxmapviewer.input.PanMouseInputListener;
import org.jxmapviewer.input.ZoomMouseWheelListenerCenter;

import javax.swing.*;
import javax.swing.event.MouseInputListener;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.ArrayList;

public class MapViewerController {

    public JPanel createMapPanel(double latitude, double longitude, String label) {
        // 1. Carte + fournisseur
        TileFactoryInfo info = new OSMTileFactoryInfo();
        DefaultTileFactory tileFactory = new DefaultTileFactory(info);
        JXMapViewer mapViewer = new JXMapViewer();
        mapViewer.setTileFactory(tileFactory);
        mapViewer.setZoom(5);

        // 2. Position centrale
        GeoPosition position = new GeoPosition(latitude, longitude);
        mapViewer.setAddressLocation(position);

        // 3. Activer drag & zoom
        MouseInputListener mia = new PanMouseInputListener(mapViewer);
        mapViewer.addMouseListener(mia);
        mapViewer.addMouseMotionListener(mia);
        mapViewer.addMouseWheelListener(new ZoomMouseWheelListenerCenter(mapViewer));
        mapViewer.addMouseListener(new CenterMapListener(mapViewer));

        // 4. Marqueur
        Set<Waypoint> waypoints = new HashSet<>();
        waypoints.add(new DefaultWaypoint(position));
        WaypointPainter<Waypoint> markerPainter = new WaypointPainter<>();
        markerPainter.setWaypoints(waypoints);

        // 5. Cercle rouge autour
        Painter<JXMapViewer> circlePainter = new Painter<>() {
            @Override
            public void paint(Graphics2D g, JXMapViewer map, int width, int height) {
                Graphics2D g2 = (Graphics2D) g.create();
                Rectangle rect = map.getViewportBounds();
                g2.translate(-rect.x, -rect.y);
                Point2D gpPt = map.getTileFactory().geoToPixel(position, map.getZoom());

                int radius = 200; // pixels
                g2.setColor(new Color(255, 0, 0, 80)); // rouge transparent
                g2.fill(new Ellipse2D.Double(gpPt.getX() - radius, gpPt.getY() - radius, radius * 2, radius * 2));
                g2.setColor(Color.RED);
                g2.setStroke(new BasicStroke(2));
                g2.draw(new Ellipse2D.Double(gpPt.getX() - radius, gpPt.getY() - radius, radius * 2, radius * 2));
                g2.dispose();
            }
        };

        // 6. Combiner les deux peintures
        List<Painter<JXMapViewer>> painters = new ArrayList<>();
        painters.add(markerPainter);
        painters.add(circlePainter);
        CompoundPainter<JXMapViewer> compoundPainter = new CompoundPainter<>(painters);
        mapViewer.setOverlayPainter(compoundPainter);

        // 7. Ajouter à un JPanel
        mapViewer.setPreferredSize(new Dimension(1000, 800));
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(mapViewer);
        panel.setPreferredSize(new Dimension(1000, 800));
        return panel;
    }
}
