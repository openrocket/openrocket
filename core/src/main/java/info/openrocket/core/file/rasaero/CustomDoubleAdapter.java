package info.openrocket.core.file.rasaero;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class CustomDoubleAdapter extends XmlAdapter<String, Double> {
    @Override
    public Double unmarshal(String s) throws Exception {
        return Double.parseDouble(s);
    }

    @Override
    public String marshal(Double aDouble) throws Exception {
        if (aDouble == null) {
            return null;
        }
        DecimalFormat df = new DecimalFormat("#.####", new DecimalFormatSymbols(Locale.US)); // RASAero has 4 decimal
                                                                                             // precision
        return df.format(aDouble);
    }
}
