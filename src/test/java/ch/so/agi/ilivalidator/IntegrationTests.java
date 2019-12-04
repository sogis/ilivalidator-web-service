package ch.so.agi.ilivalidator;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.File;
import java.io.IOException;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import io.restassured.response.Response;

/*
 * This class can be inherited by other classes to run 
 * integration tests:
 * - Spring Boot
 * - Docker Image
 */
public abstract class IntegrationTests {
	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();

	/*
	 * Simple index.html test.
	 */
	@Test
	public void indexPageTest() {				
		given().
		when().
        	get("/ilivalidator/").
        then().
            statusCode(200).
            body("html.head.title", equalTo("ilivalidator web service"));
	}
	
    /*
     * Test if version.txt is available.
     */
    @Test
    public void versionPageTest() {               
        given().
        when().
            get("/ilivalidator/version.txt").
        then().
            statusCode(200).
            body(containsString("Revision")).
            body(containsString("Application-name"));
    }

	/*
	 * We push the upload button but without sending a file
	 * to validate. It should redirect to the starting page.
	 * Not sure about how to implement this in rest assured:
	 * Now I create an empty file and checking the file size
	 * in the relevant if-clause. Testing the body seems
	 * not work though...
	 */
	@Test
	public void noFileUploadTest() throws IOException {
		File file = tempFolder.newFile("tempFile.txt");

		given().
			multiPart("file", file).
		when().
			post("/ilivalidator/").
		then().
	    	statusCode(302);//.
	    	//body("html.head.title", equalTo("ilivalidator web service")).log().all();
	}	
	
	/*
	 * Upload a text file with nonsense content and
	 * provoke a iox exception.
	 */
	@Test
	public void uploadNonsenseFileTest() throws IOException {
		File file = new File("src/test/data/nonsense.txt");

		given().
			multiPart("file", file).
		when().
			post("/ilivalidator/").
		then().
	    	statusCode(400).
	    	body(containsString("could not parse file: nonsense.txt"));
	}	
	
	@Test
	public void successfulValidationTest() {
		File file = new File("src/test/data/ch_254900.itf");
		
		given().
			multiPart("file", file).
		when().
			post("/ilivalidator/").
		then().
			statusCode(200).
			body(containsString("...validation done"));
	}
	
	@Test
	public void unsuccessfulValidationTest() {
		File file = new File("src/test/data/ch_254900_error.itf");
		
		given().
			multiPart("file", file).
		when().
			post("/ilivalidator/").
		then().
			statusCode(200);//.
			//body(containsString("...validation failed"));
	}
	
	/*
	 * INTERLIS transfer file contains some errors but these are ignored
	 * or reduced to warnings by configuration file. Hence the validation
	 * must be successful.
	 */
	@Ignore("https://github.com/claeis/ilivalidator/issues/214")
	@Test
	public void successfulValidationTestWithConfigFile() {
		File file = new File("src/test/data/Obuc_Mutation_948_2014_07_17_errors.xml");
		
		given().
			param("configFile", "on").
			multiPart("file", file).
		when().
			post("/ilivalidator/").
		then().
			statusCode(200).
			body(containsString("Warning: line")).
			body(containsString("...validation done")).
			body(containsString("Info: configFile"));
	}
	
	/*
	 * There is no according configuration file to the INTERLIS
	 * transfer/model file. Nonetheless the validation is done.
	 * "Info: configFile" should not be in the output even
	 * with 'configFile' = 'on'.
	 */
	// not successfull anymore because of all the "must not have an OID "
	@Test
	public void successfulValidationTestWithoutConfigFile() {
		File file = new File("src/test/data/ch_254900.itf");
				
		Response response =
				given().
					param("configFile", "on").
					multiPart("file", file).
				when().
					post("/ilivalidator/").
				then().
					statusCode(200).
				and().
					body(containsString("...validation done")).
				and().
					extract()
				.response();
		
		// assertThat: http://www.vogella.com/tutorials/Hamcrest/article.html  
        assertThat(response.asString().indexOf("Info: configFile"), is(-1));		
	}
	

	/*
	 * The additional constraints are defined in a separate 
	 * model file. There must be also a configuration file that
	 * makes ilivalidator aware of the additional model.
	 */
	@Test	
	public void additionalConstraintsValidationTestSuccess() {
		File file = new File("src/test/data/2457_Messen_nachher.xtf");
		
		given().
			param("configFile", "on").
			multiPart("file", file).
		when().
			post("/ilivalidator/").
		then().
			statusCode(200).
			body(containsString("Info: configFile")).
            body(containsString("additional model SO_Nutzungsplanung_20171118_Validierung_20190129")).
			body(containsString("...validation done"));
	}
	
	/*
	 * The additional constraints are defined in a separate 
	 * model file. There must be also a configuration file that
	 * makes ilivalidator aware of the additional model.
	 */
	@Test	
	public void additionalConstraintsValidationTestFail() {
		File file = new File("src/test/data/exp1_nplwis_20171213A_error.xtf");
		
		given().
			param("configFile", "on").
			multiPart("file", file).
		when().
			post("/ilivalidator/").
		then().
			statusCode(200).
			body(containsString("Info: configFile")).
            body(containsString("additional model SO_Nutzungsplanung_20171118_Validierung_20190129")).
            body(containsString("Attributwert Bezeichnung ist nicht identisch zum Objektkatalog")).
            body(containsString("Attribut 'Gemeinde' muss definiert sein.")).
            body(containsString("Attribut 'Kanton' muss definiert sein.")).
            body(containsString("Attribut 'Rechtsvorschrift' muss definiert sein.")).
            body(containsString("Attribut 'OffiziellerTitel' muss definiert sein.")).
            body(containsString("Dokument 'https://geo.so.ch/docs/ch.so.arp.zonenplaene/Zonenplaene_pdf/109-Wisen/Reglemente/109_ZR-NOT-FOUND.pdf' wurde nicht gefunden")).
			body(containsString("...validation failed"));
	}
	
	/*
	 * When "Externe Objekte prüfen" is set, some errors must be found since the
	 * objects are missing in the file.
	 */
	@Test
	public void allObjectsAccessibleTestFail() {
	    File file = new File("src/test/data/npl_niederbuchsiten_vor_OP_external_object_error.xtf");

       given().
           param("allObjectsAccessible", "on").
           multiPart("file", file).
       when().
           post("/ilivalidator/").
       then().
           statusCode(200).
           body(containsString("No object found with OID A89F574F-CB0B-4968-BED0-5811440ACEC9")).
           body(containsString("...validation failed"));
	}
	
	/*
	 * Document cycle check aka "HinweisWeitereDokumente".
	 */
	@Test
	public void documentCycleCheckTestFail() {
        File file = new File("src/test/data/2408_2019-05-02_formatiert_cycle.xtf");
        
        given().
            param("allObjectsAccessible", "on").
            multiPart("file", file).
       when().
            post("/ilivalidator/").
       then().
            statusCode(200).
            body(containsString("(DE8010C7-7255-4CDB-B361-417460CF9136 <-> 070D7074-336E-4AA4-96CC-9029D6F6E6CA) is part of a cycle: CB25AE37-3DA0-4DED-B051-A524B9A1F33D,DE8010C7-7255-4CDB-B361-417460CF9136,070D7074-336E-4AA4-96CC-9029D6F6E6CA.")).
            body(containsString("duplicate edge found: 42")).
            body(containsString("...validation failed"));
	}
	
    @Test
    public void unsuccessfulValidationTest_Nplso() {
        File file = new File("src/test/data/2405.xtf");
        
        given().
            multiPart("file", file).
        when().
            post("/ilivalidator/").
        then().
            statusCode(200).
            body(containsString("Error: line 451: SO_Nutzungsplanung_20171118.Rechtsvorschriften.HinweisWeitereDokumente: (325CA1F1-17F5-4F19-A2E4-ECD942DB6DCA <-> E9597D3A-90CD-4175-97B5-CFEAE56CB7BE) is part of a cycle: E9597D3A-90CD-4175-97B5-CFEAE56CB7BE,325CA1F1-17F5-4F19-A2E4-ECD942DB6DCA.")).
            body(containsString("Error: line 178586: SO_Nutzungsplanung_20171118.Nutzungsplanung.Ueberlagernd_Flaeche: tid 59BDF2E0-E5E7-49B9-B6BA-583BE13152C7: Set Constraint SO_Nutzungsplanung_20171118.Nutzungsplanung.Ueberlagernd_Flaeche.laermempfindlichkeitsAreaCheck is not true.")).
            body(containsString("Error: line 46065: SO_Nutzungsplanung_20171118.Nutzungsplanung.Typ_Grundnutzung: tid F8DE04B4-2E51-4B53-97DD-959A2B47242C: Typ 'N169_weitere_eingeschraenkte_Bauzonen' (Typ_Grundnutzung) ist mit keinem Dokument verkn")).
            body(containsString("Error: line 192: SO_Nutzungsplanung_20171118.Rechtsvorschriften.Dokument: tid 9C185FF7-B78F-445D-8868-905A569BA16C: Dokument 'https://geo.so.ch/docs/ch.so.arp.zonenplaene/Zonenplaene_pdf/78-Niederbuchsiten/Entscheide/78-10-E.pdf' wurde nicht gefunden.")).
            body(containsString("...validation failed"));
    }
}
