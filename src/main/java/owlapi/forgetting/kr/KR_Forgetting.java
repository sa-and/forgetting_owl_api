package owlapi.forgetting.kr;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Set;

import openllet.owlapi.explanation.PelletExplanation;
import openllet.owlapi.OpenlletReasoner;
import openllet.owlapi.OpenlletReasonerFactory;

import javax.annotation.Nonnull;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;

public class KR_Forgetting {


	public static void main(String[] args) throws OWLException, InstantiationException, IllegalAccessException,
			ClassNotFoundException, IOException {
		//String reasonerFactoryClassName = null;

		// We first need to obtain a copy of an OWLOntologyManager, which, as the name suggests, manages a set of ontologies.
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

		// We load an ontology from the URI specified on the command line
		@Nonnull
		String ontologyPath = args[0];

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

		String input1 = args[1].toLowerCase();

		switch(input1) {
			case "printallsubclasses":
				getAllSubClasses(ontology);
				break;
			case "saveallsubclasses":
				getAllSubClasses(ontology, parentDir);
				break;
			default:
		}
		// READ IN S FROM THE file
		OWLOntology ontology2 = manager.loadOntologyFromOntologyDocument(ontologyDocument);

		// FOR EACH AXIOM GET ALL EXPLANATIONS
		for (final OWLSubClassOfAxiom subsumption : ontology2.getAxioms(AxiomType.SUBCLASS_OF)){
			String input2 = args[2].toLowerCase();
			switch (input2) {
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


	@SuppressWarnings("deprecation")
	public static void getAllSubClasses(OWLOntology myOntology) {
		for (final OWLSubClassOfAxiom subClass : myOntology.getAxioms(AxiomType.SUBCLASS_OF)){
			if (subClass.getSuperClass() instanceof OWLClass && subClass.getSubClass() instanceof OWLClass){
				System.out.println(subClass.getSubClass() + " <http://www.w3.org/2000/01/rdf-schema#subClassOf> " + subClass.getSuperClass());
			}
		}
	}


	@SuppressWarnings("deprecation")
	public static void getAllSubClasses(OWLOntology myOntology, String parentDir) throws IOException {
		File subClassFile = new File(parentDir+"/subClasses.nt");
		subClassFile.createNewFile();
		FileOutputStream fos = new FileOutputStream(subClassFile, false);

		for (final OWLSubClassOfAxiom subClass : myOntology.getAxioms(AxiomType.SUBCLASS_OF)){
			if (subClass.getSuperClass() instanceof OWLClass && subClass.getSubClass() instanceof OWLClass){
				String subClassStatement = subClass.getSubClass() + " <http://www.w3.org/2000/01/rdf-schema#subClassOf> " + subClass.getSuperClass() + " .";
				fos.write(subClassStatement.getBytes());
				fos.write(System.lineSeparator().getBytes());
			}
		}
		System.out.println("/n");
		System.out.println("--------");
		System.out.println("Done! All subClass statements are saved at file: " + subClassFile);
		fos.close();
	}

	public static void getAllExplanations(OWLOntology myOntology, OWLClassExpression subClass,OWLClassExpression superClass){

		// Starting up the Pellet Explanation module.
		PelletExplanation.setup();
		// Create the reasoner and load the ontology with the open pellet reasoner.
		OpenlletReasoner reasoner = OpenlletReasonerFactory.getInstance().createReasoner(myOntology);

		// Create an Explanation reasoner with the Pellet Explanation and the Openllet Reasoner modules.
		PelletExplanation explanationsGenerator = new PelletExplanation(reasoner);

		Set<Set<OWLAxiom>> explanations = explanationsGenerator.getSubClassExplanations(subClass, superClass);

		for(Set<OWLAxiom> Explanation: explanations) {
			for (OWLAxiom rule : Explanation) {
				System.out.println(rule.toString());
			}
		}

	}

	public static void getAllExplanations(OWLOntology myOntology, OWLClassExpression subClass,OWLClassExpression superClass, String parentDir) throws IOException {
		File explanationsFile = new File(parentDir+"/explanation.txt");
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



