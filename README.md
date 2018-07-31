#  Track A -- Processing of bibliographical data and citations from PDF using GROBID and external DARIAH services -- :notebook_with_decorative_cover:

In order to promote [DARIAH](https://www.dariah.eu/activities/projects-and-affiliations/desir/) tools, services, and initiative collaborations, DESIR organises a series dissemination events, one of them is the [code sprint](https://desircodesprint.sciencesconf.org/) event which will be held on 31th July - 2nd August 2018 in Berlin. As one of events that is organised by the DESIR project, the code sprint this year take *Bibliographical metadata: Citations and References* as the main subject. To support this theme, **track A** focuses on the use and the exploration of [Grobid](https://github.com/kermitt2/grobid) as a tool for extracting the bibliographical and citations data of the Pdf scientific files. 

# Goal
This track has three goals: 
1) To explore and to improve the usability of Grobid as a tool for extracting Pdf files, particularly scientific articles; 
2) To enrich the extracted data from Grobid with some other information extracted from services other than Grobid; 
3) To visualise the extracted data in the Pdf files on the fly.  

# Grobid at a Glance
GROBID is a machine learning library for extracting, parsing and re-structuring raw documents, such as PDF documents, into structured TEI-encoded ones. 
Firstly developed in 2008 as a hobby, Grobid has become a state-of-the-art open source library for extracting metadata from technical and scientific documents in PDF format (Lipinski:2013) (Tkaczyk:2018).

With the exploration of a fully automated solution relying on machine learning (Linear Conditional Random Fields) models, Grobid works beyond than just for extracting simple bibliographic, but more than that, it works for reconstructing the logical structure of a raw document needed for large scale advanced digital library processes. 

Grobid's environment includes a comprehensive web service API, a batch processing, a JAVA API, a generic evaluation framework, and the semi-automatic generation of training data. The GROBID Web API provides a simple and efficient way to use. Furthermore, for production and benchmarking, it’s strongly recommended to use this web service mode on a multi-core machine and to avoid running GROBID in the batch mode.

The library is integrated today in various commercial and public scientific services such as ResearchGate, Mendeley, CERN Inspire and the HAL national publication repository in France. It is used on a daily basis by thousands of researchers and engineers. Since 2011, the library is open source under an Apache 2 license.

# External services
These are the external services we choosen to be integrated, the list can be ehanced by the participants: 
 - [entity-fishing](http://github.com/kermitt2/entity-fishing) a service for extracting and resolving entities against Wikipedia and Wikidata
 - entity-cooking: a (still work in progress) service to disambiguate authors and organisations against [HAL](http://hal.inria.fr)
 - add your own...

# Preparation before the track
For participating in this track, there are some preparations needed :
1) Participants need to make sure that they have already had some development tools needed whether with Java or with Python on their machine. 

2) For participants who use Java 
   * IDE: choose your own, we recommend Intellij CE
   * Java 8 (openjdk latest update)
   * Gradle, to build `./gradlew build` 

3) For participants who use Python
   * IDE: choose your own, we recommend PyCharm 
   * Python 3
   * [Virtualenv](https://virtualenv.pypa.io/en/stable/)
   * the python code is located under `src/main/python` the bottle service can be started by `python src/main/python/RootPage.py 8080` (before make sure you have ran pip to install the dependencies) 

4) Access to Github services.

# Steps
The participants for this track will involved themselves in several steps:
1) Data extraction by using Grobid
- (30 science articles in 5 languages and in Pdf format are already prepared. They can be found in `/Pdf Source Files/`)
   * Pdf files extraction into XML-TEI data format
   * Connection and data processing through the REST API
        
2) Visualisation of data extracted from Grobid 
   * Visualisation of the data extracted from the first step in the Pdf files by using Grobid as a library, for instance by using LayoutToken object in Grobid. 

3) Data enrichment  
   *  Data enrichment with some other data extracted from other services, for instance from HAL or Geonames.

4) (Bonus) Visualisation of concept and entities
   
   ```This is a bonus step if participants can finish all the 3 previous steps before the workshop ends. Visualization results may vary depending on the data collected.```
   * Data extraction by using [Entity-Fishing](https://github.com/kermitt2/nerd)   
   * Data visualisation based on the extracted data from several services in Pdf files
   

## Contact
For more information, do not hesitate to contact us: 
- Luca Foppiano (<luca.foppiano@inria.fr>)
- Tanti Kristanti (<tanti.kristanti@inria.fr>)