package owlapi.forgetting.kr;

import openllet.owlapi.OpenlletReasoner;
import openllet.owlapi.OpenlletReasonerFactory;
import openllet.owlapi.explanation.PelletExplanation;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Set;
import java.util.stream.Stream;

public class KR_AllExplanations {

    public static void main(String[] args) throws OWLException, InstantiationException, IllegalAccessException,
            ClassNotFoundException, IOException {
        // We first need to obtain a copy of an OWLOntologyManager, which, as the name suggests, manages a set of ontologies.
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

        // We load an ontology from the URI specified on the command line
        @Nonnull
        String ontologyPath = args[0];
        String subsumptionPath = args[1];

        System.out.println("Starting Program...");
        System.out.println("--------------------");
        System.out.println("Loading ontology located at: " + ontologyPath);
        System.out.println("");

        // Now load the ontology.
        File ontologyDocument = new File(ontologyPath);
        OWLOntology ontology = manager.loadOntologyFromOntologyDocument(ontologyDocument);

        // Report information about the ontology
        System.out.println("Ontology Loaded!");
        System.out.println("Format : " + manager.getOntologyFormat(ontology));
        String parentDir = ontologyDocument.getParent();
        System.out.println("Parent Directory: " + parentDir);

        // Loading the subsumptions
        System.out.println("--------------------");
        System.out.println("Loading subsumptions located at: " + subsumptionPath);

        // READ IN S FROM THE file
        File subsumptionDocument = new File(subsumptionPath);
        OWLOntology subsumptions = manager.loadOntologyFromOntologyDocument(subsumptionDocument);

        // Report subsumption ontology
        System.out.println("Subsumptions Loaded!");
        System.out.println("Format : " + manager.getOntologyFormat(subsumptions));

        String input1 = args[2].toLowerCase();
        // FOR EACH AXIOM GET ALL EXPLANATIONS
        Stream<OWLSubClassOfAxiom> subsumptionList = subsumptions.axioms(AxiomType.SUBCLASS_OF);
        for (OWLSubClassOfAxiom subsumption : (Iterable<OWLSubClassOfAxiom>) subsumptionList::iterator) {
            switch (input1) {
                case "printallexplanations":
                    getAllExplanations(ontology, subsumption.getSubClass(), subsumption.getSuperClass());
                    break;
                case "saveallexplanations":
                    getAllExplanations(ontology, subsumption.getSubClass(), subsumption.getSuperClass(), parentDir);
                    break;
                default:
            }
        }

    }

    public static void getAllExplanations(OWLOntology myOntology, OWLClassExpression subClass, OWLClassExpression superClass){

        // Starting up the Pellet Explanation module.
        PelletExplanation.setup();
        // Create the reasoner and load the ontology with the open pellet reasoner.
        OpenlletReasoner reasoner = OpenlletReasonerFactory.getInstance().createReasoner(myOntology);

        // Create an Explanation reasoner with the Pellet Explanation and the Openllet Reasoner modules.
        PelletExplanation explanationsGenerator = new PelletExplanation(reasoner);

        Set<Set<OWLAxiom>> explanations = explanationsGenerator.getSubClassExplanations(subClass, superClass);

        for(Set<OWLAxiom> Explanation: explanations) {
            System.out.println("\nExplanation:\n");
            for (OWLAxiom rule : Explanation) {
                System.out.println(rule.toString());
            }
        }

    }

    public static void getAllExplanations(OWLOntology myOntology, OWLClassExpression subClass,OWLClassExpression superClass, String parentDir) throws IOException {
        File explanationsFile = new File(parentDir+"/explanation.owl");
        explanationsFile.createNewFile();
        FileOutputStream fos = new FileOutputStream(explanationsFile, false);

        // Starting up the Pellet Explanation module.
        PelletExplanation.setup();
        // Create the reasoner and load the ontology with the open pellet reasoner.
        OpenlletReasoner reasoner = OpenlletReasonerFactory.getInstance().createReasoner(myOntology);

        // Create an Explanation reasoner with the Pellet Explanation and the Openllet Reasoner modules.
        PelletExplanation explanationsGenerator = new PelletExplanation(reasoner);

        Set<Set<OWLAxiom>> explanations = explanationsGenerator.getSubClassExplanations(subClass, superClass);

        for(Set<OWLAxiom> Explanation: explanations) {
            for (OWLAxiom rule : Explanation) {
                fos.write(rule.toString().getBytes());
                fos.write(System.lineSeparator().getBytes());
            }
        }

    }
}
