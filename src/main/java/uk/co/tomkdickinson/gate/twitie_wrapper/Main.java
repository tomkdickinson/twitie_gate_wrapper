package uk.co.tomkdickinson.gate.twitie_wrapper;

import gate.*;
import gate.corpora.DocumentContentImpl;
import gate.corpora.DocumentImpl;
import gate.creole.ResourceInstantiationException;
import gate.persist.PersistenceException;
import gate.util.GateException;
import gate.util.persistence.PersistenceManager;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Created by tkd29 on 14/05/2017.
 */
public class Main {

    private CorpusController twitieController;

    public void initGate() {
        try {
            Gate.init();
        } catch (GateException e) {
            e.printStackTrace();
        }
    }

    public void initTwitie() {
        System.out.println("Initializing Twittie");
        try {
            File twitiePlugin = new ClassPathResource("twitie.gapp").getFile();
            twitieController = (CorpusController) PersistenceManager.loadObjectFromFile(twitiePlugin);
        } catch (IOException | ResourceInstantiationException | PersistenceException e) {
            e.printStackTrace();
        }
    }

    public String processString(String text) {
        try {
            Corpus corpus = Factory.newCorpus("TwitIE Corpus");
            Document d = new DocumentImpl();
            d.setContent(new DocumentContentImpl(text));
            corpus.add(d);
            twitieController.setCorpus(corpus);
            twitieController.execute();
            processResults(corpus);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public void processResults(Corpus corpus) {
        Iterator iterator = corpus.iterator();
        while(iterator.hasNext()) {
            Document doc = (Document) iterator.next();
            doc.setPreserveOriginalContent(true);
            AnnotationSet annotationSet = doc.getAnnotations();
            Set annotationTypesRequired = new HashSet();
            annotationTypesRequired.add("Token");
            List<Annotation> tokens = new ArrayList<>(annotationSet.get(annotationTypesRequired));

            tokens.sort((a, b) -> {
                if(a.getStartNode().getOffset() < b.getStartNode().getOffset()) {
                    return -1;
                } else if(a.getStartNode().getOffset() > b.getStartNode().getOffset()) {
                    return 1;
                } else {
                    return 0;
                }
            });

            List<String> annotationLine = new ArrayList<>();
            tokens.forEach((annotation -> {
                List<String> tokenAnnotations = new ArrayList<>();
                tokenAnnotations.add((String) annotation.getFeatures().get("string"));
                tokenAnnotations.add((String) annotation.getFeatures().get("category"));
                tokenAnnotations.add((String) annotation.getFeatures().get("kind"));
                annotationLine.add(String.join("/", tokenAnnotations));
            }));
            System.out.println(String.join("\t", annotationLine));
        }
    }

    public static void main(String[] args) {

        Main main = new Main();
        main.initGate();
        main.initTwitie();
        main.processString("#whataweekend had/ an amazing time this saturday :)");
    }
}
