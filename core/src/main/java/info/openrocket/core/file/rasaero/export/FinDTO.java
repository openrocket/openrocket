package info.openrocket.core.file.rasaero.export;

import info.openrocket.core.file.rasaero.CustomDoubleAdapter;
import info.openrocket.core.file.rasaero.RASAeroCommonConstants;
import info.openrocket.core.l10n.Translator;
import info.openrocket.core.logging.ErrorSet;
import info.openrocket.core.logging.WarningSet;
import info.openrocket.core.rocketcomponent.TrapezoidFinSet;
import info.openrocket.core.rocketcomponent.position.AxialMethod;
import info.openrocket.core.file.rasaero.export.RASAeroSaver.RASAeroExportException;
import info.openrocket.core.startup.Application;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import info.openrocket.core.file.rasaero.CustomDoubleAdapter;

@JacksonXmlRootElement(localName = RASAeroCommonConstants.FIN)
public class FinDTO {
    @JacksonXmlProperty(localName = RASAeroCommonConstants.FIN_COUNT)
    private int count;
    @JacksonXmlProperty(localName = RASAeroCommonConstants.FIN_CHORD)
    @JsonSerialize(using = CustomDoubleAdapter.Serializer.class)
    private Double chord;
    @JacksonXmlProperty(localName = RASAeroCommonConstants.FIN_SPAN)
    @JsonSerialize(using = CustomDoubleAdapter.Serializer.class)
    private Double span;
    @JacksonXmlProperty(localName = RASAeroCommonConstants.FIN_SWEEP_DISTANCE)
    @JsonSerialize(using = CustomDoubleAdapter.Serializer.class)
    private Double sweepDistance;
    @JacksonXmlProperty(localName = RASAeroCommonConstants.FIN_TIP_CHORD)
    @JsonSerialize(using = CustomDoubleAdapter.Serializer.class)
    private Double tipChord;
    @JacksonXmlProperty(localName = RASAeroCommonConstants.FIN_THICKNESS)
    @JsonSerialize(using = CustomDoubleAdapter.Serializer.class)
    private Double thickness;
    @JacksonXmlProperty(localName = RASAeroCommonConstants.FIN_LE_RADIUS)
    @JsonSerialize(using = CustomDoubleAdapter.Serializer.class)
    private Double LERadius = 0.0d; // Leading edge radius
    @JacksonXmlProperty(localName = RASAeroCommonConstants.LOCATION)
    @JsonSerialize(using = CustomDoubleAdapter.Serializer.class)
    private Double location;
    @JacksonXmlProperty(localName = RASAeroCommonConstants.AIRFOIL_SECTION)
    private String airfoilSection;
    @JacksonXmlProperty(localName = RASAeroCommonConstants.FIN_FX1)
    @JsonSerialize(using = CustomDoubleAdapter.Serializer.class)
    private Double FX1 = 0.0d;
    @JacksonXmlProperty(localName = RASAeroCommonConstants.FIN_FX3)
    @JsonSerialize(using = CustomDoubleAdapter.Serializer.class)
    private Double FX3 = 0.0d;

    @JsonIgnore
    private static final Translator trans = Application.getTranslator();

    /**
     * We need a default no-args constructor.
     */
    public FinDTO() {
    }

    public FinDTO(TrapezoidFinSet fin, WarningSet warnings, ErrorSet errors) throws RASAeroExportException {
        int finCount = fin.getFinCount();
        if (finCount < 3 || finCount > 8) {
            throw new RASAeroExportException(
                    String.format(trans.get("RASAeroExport.error20"), fin.getName()));
        }

        setCount(fin.getFinCount());
        setChord(fin.getRootChord() * RASAeroCommonConstants.OPENROCKET_TO_RASAERO_LENGTH);
        setTipChord(fin.getTipChord() * RASAeroCommonConstants.OPENROCKET_TO_RASAERO_LENGTH);
        setSpan(fin.getSpan() * RASAeroCommonConstants.OPENROCKET_TO_RASAERO_LENGTH);
        setSweepDistance(fin.getSweep() * RASAeroCommonConstants.OPENROCKET_TO_RASAERO_LENGTH);
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
