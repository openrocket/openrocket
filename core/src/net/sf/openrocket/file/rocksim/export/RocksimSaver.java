package net.sf.openrocket.file.rocksim.export;

import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.document.StorageOptions;
import net.sf.openrocket.file.RocketSaver;
import net.sf.openrocket.logging.LogHelper;
import net.sf.openrocket.rocketcomponent.BodyTube;
import net.sf.openrocket.rocketcomponent.NoseCone;
import net.sf.openrocket.rocketcomponent.Rocket;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.rocketcomponent.Stage;
import net.sf.openrocket.rocketcomponent.Transition;
import net.sf.openrocket.startup.Application;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.util.List;

/**
 */
public class RocksimSaver extends RocketSaver {

    private static final LogHelper log = Application.getLogger();

    public String marshalToRocksim(OpenRocketDocument doc) {

        try {
            JAXBContext binder = JAXBContext.newInstance(RocksimDocumentDTO.class);
            Marshaller marshaller = binder.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            StringWriter sw = new StringWriter();

            marshaller.marshal(toRocksimDocumentDTO(doc), sw);
            return sw.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public void save(OutputStream dest, OpenRocketDocument doc, StorageOptions options) throws IOException {
        log.info("Saving .rkt file");

        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(dest, "UTF-8"));
        writer.write(marshalToRocksim(doc));
        writer.flush();
        writer.close();
    }

    @Override
    public long estimateFileSize(OpenRocketDocument doc, StorageOptions options) {
        return marshalToRocksim(doc).length();
    }

    private RocksimDocumentDTO toRocksimDocumentDTO(OpenRocketDocument doc) {
        RocksimDocumentDTO rsd = new RocksimDocumentDTO();

        rsd.setDesign(toRocksimDesignDTO(doc.getRocket()));

        return rsd;
    }

    private RocksimDesignDTO toRocksimDesignDTO(Rocket rocket) {
        RocksimDesignDTO result = new RocksimDesignDTO();
        result.setDesign(toRocketDesignDTO(rocket));
        return result;
    }

    private RocketDesignDTO toRocketDesignDTO(Rocket rocket) {
        RocketDesignDTO result = new RocketDesignDTO();
        result.setName(rocket.getName());
        int stageCount = rocket.getStageCount();
        result.setStageCount(stageCount);
        if (stageCount > 0) {
            result.setStage3(toStageDTO(rocket.getChild(0).getStage()));
        }
        if (stageCount > 1) {
            result.setStage2(toStageDTO(rocket.getChild(1).getStage()));
        }
        if (stageCount > 2) {
            result.setStage1(toStageDTO(rocket.getChild(2).getStage()));
        }
        return result;
    }

    private StageDTO toStageDTO(Stage stage) {
        StageDTO result = new StageDTO();

        List<RocketComponent> children = stage.getChildren();
        for (int i = 0; i < children.size(); i++) {
            RocketComponent rocketComponents = children.get(i);
            if (rocketComponents instanceof NoseCone) {
                result.addExternalPart(toNoseConeDTO((NoseCone) rocketComponents));
            } else if (rocketComponents instanceof BodyTube) {
                result.addExternalPart(toBodyTubeDTO((BodyTube) rocketComponents));
            } else if (rocketComponents instanceof Transition) {
                result.addExternalPart(toTransitionDTO((Transition) rocketComponents));
            }
        }
        return result;
    }

    private NoseConeDTO toNoseConeDTO(NoseCone nc) {
        return new NoseConeDTO(nc);
    }

    private BodyTubeDTO toBodyTubeDTO(BodyTube bt) {
        return new BodyTubeDTO(bt);
    }

    private TransitionDTO toTransitionDTO(Transition tran) {
        return new TransitionDTO(tran);
    }
}
