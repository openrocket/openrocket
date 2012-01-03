package net.sf.openrocket.android.thrustcurve;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Vector;

import org.xml.sax.Attributes;

import android.sax.Element;
import android.sax.RootElement;
import android.sax.StartElementListener;
import android.util.Log;
import android.util.Xml;

class RSEBurnFile extends MotorBurnFile {

	private final static String TAG = "RSEBurnFile";

	static void parse( MotorBurnFile that, String filecontents ) {

		parse(that, new ByteArrayInputStream(filecontents.getBytes()) );
	}

	private final static String root_tag = "engine-database";
	private final static String engine_list_tag = "engine-list";
	private final static String engine_tag = "engine";

	private final static String delays_attr = "delays";
	private final static String len_attr = "len";
	private final static String propwgt_attr = "propWt";
	private final static String totwgt_attr = "initWt";

	private final static String data_tag = "data";
	private final static String eng_data_tag = "eng-data";

	private final static String time_attr="t";
	private final static String force_attr="f";

	static void parse( final MotorBurnFile that, InputStream in ) {

		RootElement rootEl = new RootElement(root_tag);
		Element engineEl = rootEl.getChild(engine_list_tag).getChild(engine_tag);

		final Vector<Double> datapoints = new Vector<Double>();
		
		Log.d(TAG,"parsing start");

		engineEl.setStartElementListener(
				new StartElementListener() {
					@Override
					public void start(Attributes arg0) {
						Log.d(TAG,"start engineEl");
						that.setPropWeightG(Double.parseDouble(arg0.getValue(propwgt_attr)));
						that.setTotWeightG(Double.parseDouble(arg0.getValue(totwgt_attr)));
						that.setLength(Float.parseFloat(arg0.getValue(len_attr)));
						that.setDelays(arg0.getValue(delays_attr));
						Log.d(TAG, "me is now " + that.toString());
					}
				}
		);

		Element datapointEl = engineEl.getChild(data_tag).getChild(eng_data_tag);
		datapointEl.setStartElementListener(
				new StartElementListener() {
					@Override
					public void start(Attributes attributes) {
						Double x = Double.parseDouble(attributes.getValue(time_attr));
						Double y = Double.parseDouble(attributes.getValue(force_attr));
						Log.d(TAG, "add data point " + x + "," + y);
						datapoints.add(x);
						datapoints.add(y);
					}
				}
		);

		try {
            Xml.parse(in, Xml.Encoding.UTF_8,  rootEl.getContentHandler());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

		that.setDatapoints(datapoints);
	}
}
//
//	<engine-database>
//	 <engine-list>
//	<engine FDiv="10" FFix="1" FStep="-1." Isp="202.11" Itot="8.919" Type="single-use" auto-calc-cg="1" auto-calc-mass="1" avgThrust="3.795" burn-time="2.35" cgDiv="10" cgFix="1" cgStep="-1." code="C4" delays="3,5,7" dia="18." exitDia="0." initWt="17." len="50." mDiv="10" mFix="1" mStep="-1." massFrac="26.47" mfg="Apogee" peakThrust="11.31" propWt="4.5" tDiv="10" tFix="1" tStep="-1." throatDia="0.">
//	<comments>Apogee C4 RASP.ENG file made from NAR published data
//	File produced September 4, 2000
//	The total impulse, peak thrust, average thrust and burn time are
//	the same as the averaged static test data on the NAR web site in
//	the certification file. The curve drawn with these data points is as
//	close to the certification curve as can be with such a limited
//	number of points (32) allowed with wRASP up to v1.6.
//	</comments>
//	<data>
//	<eng-data cg="25." f="0." m="4.5" t="0."/>
//	<eng-data cg="25." f="3.23" m="4.48533" t="0.018"/>
//	<eng-data cg="25." f="6.874" m="4.42671" t="0.041"/>
//	<eng-data cg="25." f="8.779" m="4.00814" t="0.147"/>
//	<eng-data cg="25." f="10.683" m="3.28643" t="0.294"/>
//	<eng-data cg="25." f="11.31" m="2.89252" t="0.365"/>
//	<eng-data cg="25." f="10.521" m="2.76585" t="0.388"/>
//	<eng-data cg="25." f="8.779" m="2.649" t="0.412"/>
//	<eng-data cg="25." f="7.04" m="2.53328" t="0.441"/>
//	<eng-data cg="25." f="4.555" m="2.46308" t="0.465"/>
//	<eng-data cg="25." f="3.479" m="2.33337" t="0.529"/>
//	<eng-data cg="25." f="2.981" m="2.1704" t="0.629"/>
//	<eng-data cg="25." f="3.23" m="2.1328" t="0.653"/>
//	<eng-data cg="25." f="2.816" m="2.03366" t="0.718"/>
//	<eng-data cg="25." f="2.733" m="1.84469" t="0.853"/>
//	<eng-data cg="25." f="2.65" m="1.5568" t="1.065"/>
//	<eng-data cg="25." f="2.567" m="1.30938" t="1.253"/>
//	<eng-data cg="25." f="2.401" m="1.05873" t="1.453"/>
//	<eng-data cg="25." f="2.484" m="0.761739" t="1.694"/>
//	<eng-data cg="25." f="2.484" m="0.636413" t="1.794"/>
//	<eng-data cg="25." f="2.733" m="0.612724" t="1.812"/>
//	<eng-data cg="25." f="2.401" m="0.575165" t="1.841"/>
//	<eng-data cg="25." f="2.401" m="0.446759" t="1.947"/>
//	<eng-data cg="25." f="2.401" m="0.246881" t="2.112"/>
//	<eng-data cg="25." f="2.401" m="0.0978809" t="2.235"/>
//	<eng-data cg="25." f="2.236" m="0.0429024" t="2.282"/>
//	<eng-data cg="25." f="1.656" m="0.0134478" t="2.312"/>
//	<eng-data cg="25." f="0.662" m="0.003507" t="2.329"/>
//	<eng-data cg="25." f="0." m="-0." t="2.35"/>
//	</data>
//	</engine>
//	 </engine-list>
//	</engine-database>
//
