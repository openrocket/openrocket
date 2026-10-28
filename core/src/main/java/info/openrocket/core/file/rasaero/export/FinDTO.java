package info.openrocket.core.file.rasaero.export;

import info.openrocket.core.file.rasaero.CustomDoubleAdapter;
import info.openrocket.core.file.rasaero.RASAeroCommonConstants;
import info.openrocket.core.l10n.Translator;
import info.openrocket.core.logging.ErrorSet;
import info.openrocket.core.logging.WarningSet;
import info.openrocket.core.rocketcomponent.FinSet;
import info.openrocket.core.rocketcomponent.TrapezoidFinSet;
import info.openrocket.core.util.CoordinateIF;
import info.openrocket.core.rocketcomponent.position.AxialMethod;
import info.openrocket.core.file.rasaero.export.RASAeroSaver.RASAeroExportException;
import info.openrocket.core.startup.Application;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlRootElement(name = RASAeroCommonConstants.FIN)
@XmlAccessorType(XmlAccessType.FIELD)
public class FinDTO {
    @XmlElement(name = RASAeroCommonConstants.FIN_COUNT)
    private int count;
    @XmlElement(name = RASAeroCommonConstants.FIN_CHORD)
    @XmlJavaTypeAdapter(CustomDoubleAdapter.class)
    private Double chord;
    @XmlElement(name = RASAeroCommonConstants.FIN_SPAN)
    @XmlJavaTypeAdapter(CustomDoubleAdapter.class)
    private Double span;
    @XmlElement(name = RASAeroCommonConstants.FIN_SWEEP_DISTANCE)
    @XmlJavaTypeAdapter(CustomDoubleAdapter.class)
    private Double sweepDistance;
    @XmlElement(name = RASAeroCommonConstants.FIN_TIP_CHORD)
    @XmlJavaTypeAdapter(CustomDoubleAdapter.class)
    private Double tipChord;
    @XmlElement(name = RASAeroCommonConstants.FIN_THICKNESS)
    @XmlJavaTypeAdapter(CustomDoubleAdapter.class)
    private Double thickness;
    @XmlElement(name = RASAeroCommonConstants.FIN_LE_RADIUS)
    @XmlJavaTypeAdapter(CustomDoubleAdapter.class)
    private Double LERadius = 0.0d; // Leading edge radius
    @XmlElement(name = RASAeroCommonConstants.LOCATION)
    @XmlJavaTypeAdapter(CustomDoubleAdapter.class)
    private Double location;
    @XmlElement(name = RASAeroCommonConstants.AIRFOIL_SECTION)
    private String airfoilSection;
    @XmlElement(name = RASAeroCommonConstants.FIN_FX1)
    @XmlJavaTypeAdapter(CustomDoubleAdapter.class)
    private Double FX1 = 0.0d;
    @XmlElement(name = RASAeroCommonConstants.FIN_FX3)
    @XmlJavaTypeAdapter(CustomDoubleAdapter.class)
    private Double FX3 = 0.0d;

    @XmlTransient
    private static final Translator trans = Application.getTranslator();

    /**
     * We need a default no-args constructor.
     */
    public FinDTO() {
    }

    /**
     * RASAero only has trapezoidal fins. Export the trapezoid with the same root chord, span, planform area and
     * planform area centroid as the fin, which keeps the fin's normal force and center of pressure close.
     *
     * @param fin the non-trapezoidal fin set
     */
    private void setEquivalentTrapezoid(FinSet fin) throws RASAeroExportException {
        CoordinateIF[] points = fin.getFinPoints();
        double rootChord = points[points.length - 1].getX() - points[0].getX();
        double span = fin.getSpan();

        // Area and area centroid of the fin outline, closed along the root
        double area = 0;
        double moment = 0;
        for (int i = 0; i < points.length; i++) {
            CoordinateIF p0 = points[i];
            CoordinateIF p1 = points[(i + 1) % points.length];
            double cross = p0.getX() * p1.getY() - p1.getX() * p0.getY();
            area += cross / 2;
            moment += (p0.getX() + p1.getX()) * cross / 6;
        }
        double centroidX = moment / area - points[0].getX();
        area = Math.abs(area);

        double tipChord = Math.max(0, 2 * area / span - rootChord);
        double sweep = (3 * (rootChord + tipChord) * centroidX
                - (rootChord * rootChord + rootChord * tipChord + tipChord * tipChord)) / (rootChord + 2 * tipChord);

        setChord(rootChord * RASAeroCommonConstants.OPENROCKET_TO_RASAERO_LENGTH);
        setTipChord(tipChord * RASAeroCommonConstants.OPENROCKET_TO_RASAERO_LENGTH);
        setSpan(span * RASAeroCommonConstants.OPENROCKET_TO_RASAERO_LENGTH);
        setSweepDistance(sweep * RASAeroCommonConstants.OPENROCKET_TO_RASAERO_LENGTH);
    }

    public FinDTO(FinSet fin, WarningSet warnings, ErrorSet errors) throws RASAeroExportException {
        int finCount = fin.getFinCount();
        if (finCount < 3 || finCount > 8) {
            throw new RASAeroExportException(
                    String.format(trans.get("RASAeroExport.error20"), fin.getName()));
        }

        setCount(fin.getFinCount());
        if (fin instanceof TrapezoidFinSet) {
            TrapezoidFinSet trapezoid = (TrapezoidFinSet) fin;
            setChord(trapezoid.getRootChord() * RASAeroCommonConstants.OPENROCKET_TO_RASAERO_LENGTH);
            setTipChord(trapezoid.getTipChord() * RASAeroCommonConstants.OPENROCKET_TO_RASAERO_LENGTH);
            setSpan(trapezoid.getSpan() * RASAeroCommonConstants.OPENROCKET_TO_RASAERO_LENGTH);
            setSweepDistance(trapezoid.getSweep() * RASAeroCommonConstants.OPENROCKET_TO_RASAERO_LENGTH);
        } else {
            setEquivalentTrapezoid(fin);
            warnings.add(String.format(trans.get("RASAeroExport.warningB3"), fin.getName()));
        }
        setThickness(fin.getThickness() * RASAeroCommonConstants.OPENROCKET_TO_RASAERO_LENGTH);
        setAirfoilSection(
                RASAeroCommonConstants.OPENROCKET_TO_RASAERO_FIN_CROSSSECTION(fin.getCrossSection(), warnings));
        setLocation((-fin.getAxialOffset(AxialMethod.BOTTOM) + fin.getLength())
                * RASAeroCommonConstants.OPENROCKET_TO_RASAERO_LENGTH);
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public Double getChord() {
        return chord;
    }

    public void setChord(Double chord) {
        this.chord = chord;
    }

    public Double getSpan() {
        return span;
    }

    public void setSpan(Double span) {
        this.span = span;
    }

    public Double getSweepDistance() {
        return sweepDistance;
    }

    public void setSweepDistance(Double sweepDistance) {
        this.sweepDistance = sweepDistance;
    }

    public Double getTipChord() {
        return tipChord;
    }

    public void setTipChord(Double tipChord) {
        this.tipChord = tipChord;
    }

    public Double getThickness() {
        return thickness;
    }

    public void setThickness(Double thickness) {
        this.thickness = thickness;
    }

    public Double getLERadius() {
        return LERadius;
    }

    public void setLERadius(Double LERadius) {
        this.LERadius = LERadius;
    }

    public Double getLocation() {
        return location;
    }

    public void setLocation(Double location) {
        this.location = location;
    }

    public String getAirfoilSection() {
        return airfoilSection;
    }

    public void setAirfoilSection(String airfoilSection) {
        this.airfoilSection = airfoilSection;
    }

    public Double getFX1() {
        return FX1;
    }

    public void setFX1(Double FX1) {
        this.FX1 = FX1;
    }

    public Double getFX3() {
        return FX3;
    }

    public void setFX3(Double FX3) {
        this.FX3 = FX3;
    }
}
