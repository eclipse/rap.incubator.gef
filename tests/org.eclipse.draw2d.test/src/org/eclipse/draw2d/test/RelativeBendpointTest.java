/*******************************************************************************
 * Copyright (c) 2008 ILOG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     ILOG - initial API and implementation
 *******************************************************************************/
package org.eclipse.draw2d.test;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.AbstractConnectionAnchor;
import org.eclipse.draw2d.BendpointConnectionRouter;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.FreeformLayeredPane;
import org.eclipse.draw2d.FreeformLayout;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.draw2d.RectangleFigure;
import org.eclipse.draw2d.RelativeBendpoint;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PrecisionPoint;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.widgets.Shell;

/**
 * @author Romain Raugi
 */
public class RelativeBendpointTest extends AbstractFixtureTestCase {

	private static class DiagramFigure extends FreeformLayeredPane {
		public DiagramFigure() {
			super();
			setLayoutManager(new FreeformLayout());
			setBackgroundColor(ColorConstants.white());
			setOpaque(true);
		}
	}

	private static class NodeFigure extends RectangleFigure {
		public NodeFigure() {
			super();
			setFill(true);
			setBackgroundColor(ColorConstants.darkGray());
		}
	}

	private static class FixedAnchor extends AbstractConnectionAnchor {
		PrecisionPoint place;

		public FixedAnchor(IFigure owner) {
			super(owner);
		}

		public Point getLocation(Point loc) {
			Rectangle r = getOwner().getBounds();
			double x = r.x + place.preciseX() * r.width;
			double y = r.y + place.preciseY() * r.height;
			Point p = new PrecisionPoint(x, y);
			getOwner().translateToAbsolute(p);
			return p;
		}

		public Point getReferencePoint() {
			return getLocation(null);
		}
	}

	private static class ConnectionFigure extends PolylineConnection {
		public ConnectionFigure() {
			setConnectionRouter(new BendpointConnectionRouter());
		}
	}

	private Shell shell;

	private DiagramFigure diagram;

	protected void setUp() throws Exception {
		super.setUp();
		shell = new Shell(display);
		shell.setSize(300, 350);
		shell.open();
		shell.setText("RelativeBendpoint Test 4 Bug 237802");
		// the diagram
		LightweightSystem lws = new LightweightSystem(shell);
		diagram = new DiagramFigure();
		lws.setContents(diagram);
	}

	protected void tearDown() throws Exception {
		shell.close();
		super.tearDown();
	}

	public void test237802() {
		// first node
		NodeFigure source = new NodeFigure();
		source.setBounds(new Rectangle(50, 50, 50, 50));
		diagram.add(source);
		// second node
		NodeFigure target = new NodeFigure();
		target.setBounds(new Rectangle(150, 200, 50, 50));
		diagram.add(target);
		// anchors
		FixedAnchor sourceAnchor, targetAnchor;
		sourceAnchor = new FixedAnchor(source);
		sourceAnchor.place = new PrecisionPoint(.5, 1.);
		targetAnchor = new FixedAnchor(target);
		targetAnchor.place = new PrecisionPoint(.5, 0.);
		// first connection
		ConnectionFigure connection = new ConnectionFigure();
		connection.setSourceAnchor(sourceAnchor);
		connection.setTargetAnchor(targetAnchor);
		diagram.add(connection);
		// bendpoints
		RelativeBendpoint bendpoint1 = new RelativeBendpoint(connection);
		RelativeBendpoint bendpoint2 = new RelativeBendpoint(connection);
		bendpoint1.setRelativeDimensions(new Dimension(0, 50), new Dimension(
				-100, -50));
		bendpoint1.setWeight(1f / 3f);
		bendpoint2.setRelativeDimensions(new Dimension(100, 50), new Dimension(
				0, -50));
		bendpoint2.setWeight(2f / 3f);
		List bendpoints = new ArrayList(2);
		bendpoints.add(bendpoint1);
		bendpoints.add(bendpoint2);
		connection.setRoutingConstraint(bendpoints);
		// expected the bendpoints to be at certain locations
		Point p1 = bendpoint1.getLocation();
		Point p2 = bendpoint2.getLocation();
		assertEquals(p1.x, 75);
		assertEquals(p1.y, 150);
		assertEquals(p2.x, 175);
		assertEquals(p2.y, 150);
	}

}