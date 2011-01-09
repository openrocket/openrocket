/*
 * PartsDetailVisitorStrategy.java
 */
package net.sf.openrocket.gui.print.visitor;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.swing.ImageIcon;

import net.sf.openrocket.gui.main.ComponentIcons;
import net.sf.openrocket.gui.print.ITextHelper;
import net.sf.openrocket.gui.print.PrintUtilities;
import net.sf.openrocket.gui.print.PrintableFinSet;
import net.sf.openrocket.material.Material;
import net.sf.openrocket.rocketcomponent.BodyComponent;
import net.sf.openrocket.rocketcomponent.BodyTube;
import net.sf.openrocket.rocketcomponent.Coaxial;
import net.sf.openrocket.rocketcomponent.EllipticalFinSet;
import net.sf.openrocket.rocketcomponent.ExternalComponent;
import net.sf.openrocket.rocketcomponent.FinSet;
import net.sf.openrocket.rocketcomponent.FreeformFinSet;
import net.sf.openrocket.rocketcomponent.InnerTube;
import net.sf.openrocket.rocketcomponent.LaunchLug;
import net.sf.openrocket.rocketcomponent.MassObject;
import net.sf.openrocket.rocketcomponent.NoseCone;
import net.sf.openrocket.rocketcomponent.RadiusRingComponent;
import net.sf.openrocket.rocketcomponent.RingComponent;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.rocketcomponent.Stage;
import net.sf.openrocket.rocketcomponent.Transition;
import net.sf.openrocket.rocketcomponent.TrapezoidFinSet;
import net.sf.openrocket.unit.Unit;
import net.sf.openrocket.unit.UnitGroup;
import net.sf.openrocket.util.Coordinate;

import com.itextpdf.text.BadElementException;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

/**
 * A visitor strategy for creating documentation about parts details.
 */
public class PartsDetailVisitorStrategy extends BaseVisitorStrategy {
	
	/**
	 * The number of columns in the table.
	 */
	private static final int TABLE_COLUMNS = 7;
	
	/**
	 * The parts detail is represented as an iText table.
	 */
	PdfPTable grid;
	
	/**
	 * Construct a strategy for visiting a parts hierarchy for the purposes of collecting details on those parts.
	 *
	 * @param doc              The iText document
	 * @param theWriter        The direct iText writer
	 * @param theStagesToVisit The stages to be visited by this strategy
	 */
	public PartsDetailVisitorStrategy(Document doc, PdfWriter theWriter, Set<Integer> theStagesToVisit) {
		super(doc, theWriter, theStagesToVisit);
		PrintUtilities.addText(doc, PrintUtilities.BIG_BOLD, "Parts Detail");
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void visit(final Stage visitable) {
		try {
			if (grid != null) {
				document.add(grid);
			}
			document.add(ITextHelper.createPhrase(visitable.getName()));
			grid = new PdfPTable(TABLE_COLUMNS);
			grid.setWidthPercentage(100);
			grid.setHorizontalAlignment(Element.ALIGN_LEFT);
		} catch (DocumentException e) {
		}
		
		List<RocketComponent> rc = visitable.getChildren();
		goDeep(rc);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void visit(final ExternalComponent visitable) {
		grid.addCell(iconToImage(visitable));
		grid.addCell(createNameCell(visitable.getName(), true));
		
		grid.addCell(createMaterialCell(visitable.getMaterial()));
		grid.addCell(ITextHelper.createCell());
		grid.addCell(createLengthCell(visitable.getLength()));
		grid.addCell(createMassCell(visitable.getMass()));
		
		List<RocketComponent> rc = visitable.getChildren();
		goDeep(rc);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void visit(final BodyComponent visitable) {
		grid.addCell(visitable.getName());
		grid.completeRow();
		List<RocketComponent> rc = visitable.getChildren();
		goDeep(rc);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void visit(final RingComponent visitable) {
		grid.addCell(iconToImage(visitable));
		grid.addCell(createNameCell(visitable.getName(), true));
		grid.addCell(createMaterialCell(visitable.getMaterial()));
		grid.addCell(createOuterDiaCell(visitable));
		grid.addCell(createLengthCell(visitable.getLength()));
		grid.addCell(createMassCell(visitable.getMass()));
		
		List<RocketComponent> rc = visitable.getChildren();
		goDeep(rc);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void visit(final InnerTube visitable) {
		grid.addCell(iconToImage(visitable));
		final PdfPCell pCell = createNameCell(visitable.getName(), true);
		grid.addCell(pCell);
		grid.addCell(createMaterialCell(visitable.getMaterial()));
		grid.addCell(createOuterDiaCell(visitable));
		grid.addCell(createLengthCell(visitable.getLength()));
		grid.addCell(createMassCell(visitable.getMass()));
		
		List<RocketComponent> rc = visitable.getChildren();
		goDeep(rc);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void visit(final LaunchLug visitable) {
		grid.addCell(iconToImage(visitable));
		grid.addCell(createNameCell(visitable.getName(), true));
		
		grid.addCell(createMaterialCell(visitable.getMaterial()));
		grid.addCell(createOuterDiaCell(visitable));
		grid.addCell(createLengthCell(visitable.getLength()));
		grid.addCell(createMassCell(visitable.getMass()));
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void visit(final Transition visitable) {
		grid.addCell(iconToImage(visitable));
		grid.addCell(createNameCell(visitable.getName(), true));
		grid.addCell(createMaterialCell(visitable.getMaterial()));
		
		Chunk fore = new Chunk("Fore Dia: " + appendLength(visitable.getForeRadius() * 2));
		fore.setFont(new Font(Font.FontFamily.HELVETICA, PrintUtilities.NORMAL_FONT_SIZE));
		Chunk aft = new Chunk("Aft Dia: " + appendLength(visitable.getAftRadius() * 2));
		aft.setFont(new Font(Font.FontFamily.HELVETICA, PrintUtilities.NORMAL_FONT_SIZE));
		final PdfPCell cell = ITextHelper.createCell();
		cell.addElement(fore);
		cell.addElement(aft);
		grid.addCell(cell);
		grid.addCell(createLengthCell(visitable.getLength()));
		grid.addCell(createMassCell(visitable.getMass()));
		
		List<RocketComponent> rc = visitable.getChildren();
		goDeep(rc);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void visit(final RadiusRingComponent visitable) {
		grid.addCell(iconToImage(visitable));
		grid.addCell(createNameCell(visitable.getName(), true));
		grid.addCell(createMaterialCell(visitable.getMaterial()));
		grid.addCell(createOuterDiaCell(visitable));
		grid.addCell(createLengthCell(visitable.getLength()));
		grid.addCell(createMassCell(visitable.getMass()));
		List<RocketComponent> rc = visitable.getChildren();
		goDeep(rc);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void visit(final MassObject visitable) {
		PdfPCell cell = ITextHelper.createCell();
		cell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
		cell.setPaddingBottom(12f);
		
		grid.addCell(iconToImage(visitable));
		final PdfPCell nameCell = createNameCell(visitable.getName(), true);
		nameCell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
		nameCell.setPaddingBottom(12f);
		grid.addCell(nameCell);
		grid.addCell(cell);
		grid.addCell(cell);
		grid.addCell(cell);
		grid.addCell(createMassCell(visitable.getMass()));
		List<RocketComponent> rc = visitable.getChildren();
		goDeep(rc);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void visit(final NoseCone visitable) {
		grid.addCell(iconToImage(visitable));
		grid.addCell(createNameCell(visitable.getName(), true));
		grid.addCell(createMaterialCell(visitable.getMaterial()));
		grid.addCell(ITextHelper.createCell(visitable.getType().getName(), PdfPCell.BOTTOM));
		grid.addCell(createLengthCell(visitable.getLength()));
		grid.addCell(createMassCell(visitable.getMass()));
		List<RocketComponent> rc = visitable.getChildren();
		goDeep(rc);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void visit(final BodyTube visitable) {
		grid.addCell(iconToImage(visitable));
		grid.addCell(createNameCell(visitable.getName(), true));
		grid.addCell(createMaterialCell(visitable.getMaterial()));
		grid.addCell(createOuterDiaCell(visitable));
		grid.addCell(createLengthCell(visitable.getLength()));
		grid.addCell(createMassCell(visitable.getMass()));
		List<RocketComponent> rc = visitable.getChildren();
		goDeep(rc);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void visit(final TrapezoidFinSet visitable) {
		visitFins(visitable);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void visit(final EllipticalFinSet visitable) {
		visitFins(visitable);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void visit(final FreeformFinSet visitable) {
		visitFins(visitable);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void close() {
		try {
			if (grid != null) {
				document.add(grid);
			}
		} catch (DocumentException e) {
			e.printStackTrace();
		}
	}
	
	private PdfPCell createOuterDiaCell(final Coaxial visitable) {
		PdfPCell result = new PdfPCell();
		Phrase p = new Phrase();
		p.setLeading(12f);
		result.setVerticalAlignment(Element.ALIGN_TOP);
		result.setBorder(Rectangle.BOTTOM);
		Chunk c = new Chunk();
		c.setFont(new Font(Font.FontFamily.HELVETICA, PrintUtilities.NORMAL_FONT_SIZE));
		c.append("Dia");
		p.add(c);
		
		c = new Chunk();
		c.setFont(new Font(Font.FontFamily.HELVETICA, PrintUtilities.SMALL_FONT_SIZE));
		c.append("out");
		p.add(c);
		
		c = new Chunk();
		c.setFont(new Font(Font.FontFamily.HELVETICA, PrintUtilities.NORMAL_FONT_SIZE));
		c.append(" " + appendLength(visitable.getOuterRadius() * 2));
		p.add(c);
		createInnerDiaCell(visitable, result);
		result.addElement(p);
		return result;
	}
	
	private void createInnerDiaCell(final Coaxial visitable, PdfPCell cell) {
		Phrase p = new Phrase();
		p.setLeading(14f);
		Chunk c = new Chunk();
		c.setFont(new Font(Font.FontFamily.HELVETICA, PrintUtilities.NORMAL_FONT_SIZE));
		c.append("Dia");
		p.add(c);
		
		c = new Chunk();
		c.setFont(new Font(Font.FontFamily.HELVETICA, PrintUtilities.SMALL_FONT_SIZE));
		c.append("in ");
		p.add(c);
		
		c = new Chunk();
		c.setFont(new Font(Font.FontFamily.HELVETICA, PrintUtilities.NORMAL_FONT_SIZE));
		c.append("  " + appendLength(visitable.getInnerRadius() * 2));
		p.add(c);
		cell.addElement(p);
	}
	
	private void visitFins(FinSet visitable) {
		
		Image img = null;
		java.awt.Image awtImage = new PrintableFinSet(visitable).createImage();
		
		Collection<Coordinate> x = visitable.getComponentBounds();
		
		try {
			img = Image.getInstance(writer, awtImage, 0.25f);
		} catch (BadElementException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		grid.addCell(iconToImage(visitable));
		grid.addCell(createNameCell(visitable.getName() + " (" + visitable.getFinCount() + ")", true));
		grid.addCell(createMaterialCell(visitable.getMaterial()));
		grid.addCell(ITextHelper.createCell("Thick: " + appendLength(visitable.getThickness()), PdfPCell.BOTTOM));
		final PdfPCell pCell = new PdfPCell();
		pCell.setBorder(Rectangle.BOTTOM);
		pCell.addElement(img);
		
		grid.addCell(ITextHelper.createCell());
		grid.addCell(createMassCell(visitable.getMass()));
		
		List<RocketComponent> rc = visitable.getChildren();
		goDeep(rc);
	}
	
	protected PdfPCell createLengthCell(double length) {
		return ITextHelper.createCell("Len: " + appendLength(length), PdfPCell.BOTTOM);
	}
	
	protected PdfPCell createMassCell(double mass) {
		return ITextHelper.createCell("Mass: " + appendMass(mass), PdfPCell.BOTTOM);
	}
	
	protected PdfPCell createNameCell(String v, boolean withIndent) {
		PdfPCell result = new PdfPCell();
		result.setBorder(Rectangle.BOTTOM);
		Chunk c = new Chunk();
		c.setFont(new Font(Font.FontFamily.HELVETICA, PrintUtilities.NORMAL_FONT_SIZE));
		if (withIndent) {
			for (int x = 0; x < (level - 2) * 10; x++) {
				c.append(" ");
			}
		}
		c.append(v);
		result.setColspan(2);
		result.addElement(c);
		return result;
	}
	
	protected PdfPCell createMaterialCell(Material material) {
		PdfPCell cell = ITextHelper.createCell();
		cell.setLeading(13f, 0);
		
		Chunk c = new Chunk();
		c.setFont(new Font(Font.FontFamily.HELVETICA, PrintUtilities.NORMAL_FONT_SIZE));
		c.append(appendMaterial(material));
		cell.addElement(c);
		Chunk density = new Chunk();
		density.setFont(new Font(Font.FontFamily.HELVETICA, PrintUtilities.SMALL_FONT_SIZE));
		density.append(appendMaterialDensity(material));
		cell.addElement(density);
		return cell;
	}
	
	protected PdfPCell iconToImage(final RocketComponent visitable) {
		final ImageIcon icon = (ImageIcon) ComponentIcons.getLargeIcon(visitable.getClass());
		try {
			Image im = Image.getInstance(icon.getImage(), null);
			im.scaleToFit(icon.getIconWidth() * 0.6f, icon.getIconHeight() * 0.6f);
			PdfPCell cell = new PdfPCell(im);
			cell.setFixedHeight(icon.getIconHeight() * 0.6f);
			cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
			cell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
			cell.setBorder(PdfPCell.NO_BORDER);
			return cell;
		} catch (BadElementException e) {
		} catch (IOException e) {
		}
		return null;
	}
	
	protected String appendLength(double length) {
		final Unit defaultUnit = UnitGroup.UNITS_LENGTH.getDefaultUnit();
		return NumberFormat.getNumberInstance().format(defaultUnit.toUnit(length)) + defaultUnit.toString();
	}
	
	protected String appendMass(double mass) {
		final Unit defaultUnit = UnitGroup.UNITS_MASS.getDefaultUnit();
		return NumberFormat.getNumberInstance().format(defaultUnit.toUnit(mass)) + defaultUnit.toString();
	}
	
	protected String appendMaterial(Material material) {
		return material.getName();
	}
	
	protected String appendMaterialDensity(Material material) {
		return " (" + material.getType().getUnitGroup().getDefaultUnit().toStringUnit(material.getDensity()) + ")";
	}
	
}
