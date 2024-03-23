package info.openrocket.core.file.rasaero;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;

public class CustomBooleanAdapter extends XmlAdapter<String, Boolean> {

    @Override
    public Boolean unmarshal(String s) throws Exception {
        return "true".equalsIgnoreCase(s);
    }

    @Override
    public String marshal(Boolean b) throws Exception {
        if (b) {
            return "True";
        }
        return "False";
    }
}
