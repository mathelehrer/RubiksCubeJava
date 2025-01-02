package com.numbercruncher.rubikscube.math;

import com.numbercruncher.rubikscube.logger.Logger;
import com.numbercruncher.rubikscube.utils.IOUtils;
import com.numbercruncher.rubikscube.xml.ErrorHandler;
import com.numbercruncher.rubikscube.xml.MinkwitzParser;
import org.xml.sax.XMLReader;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import static com.numbercruncher.rubikscube.utils.StringUtils.*;
import static com.numbercruncher.rubikscube.utils.StringUtils.startTag;

/**
 * The class StabilizerChain
 *
 * @author NumberCruncher
 * @since 2024-12-30
 * @version 2024-12-30
 */
public class MinkwitzChain {

    /*****************************
     **** Attribute **************
     *****************************/

    public static final String XML_HEADER="<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
    public static enum Tag{stabilizer, orbit, representatives, representative,permutation, word};


    private final List<Byte> orbit;
    private final Map<Byte,GroupElement> cosetRepresentative;
    private MinkwitzChain minkwitzChain;
    private final String name;

    /*****************************
     **** Konstruktor*************
     *****************************/


    /**
     * Constructs a MinkwitzChain object based on the provided StabilizerChain.
     * This constructor initializes the `orbit` field from the given StabilizerChain's orbit.
     * If the given StabilizerChain is not the last in the sequence, it recursively sets the
     * `stabilizer` field using a new MinkwitzChain created from the StabilizerChain's stabilizer.
     *
     * Therefore, the orbit structure of the stabilizer chain is used to construct the structure of hte Minkwitz chain.
     * The populate algorithm is used to populate the empty generator and representative structure with group elements.
     *
     * @param chain the StabilizerChain object used to initialize the MinkwitzChain's fields.
     *              Must provide valid `orbit` and optionally a `stabilizer`.
     */
    public MinkwitzChain(StabilizerChain chain, List<GroupElement> generators, String name) {
        this.name = name;
        GroupElement one = generators.get(0).multiply(generators.get(0).inverse());
        this.orbit = chain.getOrbit();
        this.cosetRepresentative = new HashMap<>();
        if (!this.orbit.isEmpty()) {
            for (Byte b : this.orbit) {
                this.cosetRepresentative.put(b,null);
            }
            this.cosetRepresentative.put(this.orbit.get(0), one);
            this.minkwitzChain = new MinkwitzChain(chain.getStabilizer(), generators);
        }

    }

    public MinkwitzChain(StabilizerChain chain, List<GroupElement> generators) {
        this(chain,generators,"UnknownMinkwitzChain");
    }

    /**
     * empty constructor
     * used as placeholder for the XML parser
     */
    public MinkwitzChain(String name){
        this.name = name;
        this.orbit = null;
        this.cosetRepresentative = null;
        this.minkwitzChain = null;
        load(name);
    }


    /**
     * Trained MinkwitzChain loaded from XML
     * @param orbit
     * @param cosetRepresentative
     * @param stabilizer
     */
    public MinkwitzChain(List<Byte> orbit, Map<Byte,GroupElement> cosetRepresentative, MinkwitzChain stabilizer,String name){
        this.name = name;
        this.orbit = orbit;
        this.cosetRepresentative = cosetRepresentative;
        this.minkwitzChain =stabilizer;
    }

    public MinkwitzChain(List<Byte> orbit, Map<Byte,GroupElement> cosetRepresentative, MinkwitzChain stabilizer){
        this.name = "";
        this.orbit = orbit;
        this.cosetRepresentative = cosetRepresentative;
        this.minkwitzChain =stabilizer;
    }

    /*****************************
     **** Getter    **************
     *****************************/


    public List<Byte> getOrbit() {
        return orbit;
    }

    public GroupElement getCosetRepresentative(Byte point){
        return cosetRepresentative.get(point);
    }

    public MinkwitzChain getMinkwitzChain(){
        return minkwitzChain;
    }

    public Map<Byte,GroupElement> getCosetRepresentatives(){
        return cosetRepresentative;
    }

    public int getNumberOfMissingElements(){
        int count=0;
        for (GroupElement value : cosetRepresentative.values()) {
            if (value == null) {
                count++;
            }
        }
        if (isLast())
            return count;

        else
            return count + this.minkwitzChain.getNumberOfMissingElements();
    }

    public double getAverageWordLength(){

        int[] data =  countWordsLetters(this);
        return (double) data[1]/data[0];

    }

    /*****************************
     **** Setter    **************
     *****************************/


    public void addCosetRepresentative(Byte point,GroupElement permutation){
        this.cosetRepresentative.put(point,permutation);
    }

    /*****************************
     **** public methods *********
     *****************************/

    /**
     * Checks whether the current stabilizer chain is the last one in the sequence.
     *
     * @return true if the stabilizer of this chain is null, indicating it is the last one; otherwise false.
     */
    public boolean isLast(){
        return this.orbit.isEmpty();
    }

    /**
     * Checks the consistency of the stabilizer chain by comparing the size of the coset representative
     * map and the size of the orbit list.
     *
     * @return true if the size of the coset representative map is equal to the size of the orbit list,
     * indicating consistency; false otherwise.
     */
    public boolean isConsistent(){
        return this.cosetRepresentative.size()==this.orbit.size();
    }

    public void save(){
        this.save("");
    }

    public void save(String params){
        URL dirURL = IOUtils.getResourcePath("minkwitz");
        String fileName = dirURL.getFile()+"/"+this.name+params+".xml";
        StringBuilder output = new StringBuilder();

        output.append(XML_HEADER);
        output.append(generateXMLString(this));

        File file = new File(fileName);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                Logger.logging(Logger.Level.warning,"Could not create file "+fileName,this);
                return;
            }
        }

        PrintWriter writer=null;
        try {
            writer = new PrintWriter(fileName, StandardCharsets.UTF_8);
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            e.printStackTrace();
            Logger.logging(Logger.Level.warning,"Could not create file "+fileName+"\n"+e.getMessage(),this);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (writer!=null) {
            writer.println(output);
            writer.close();
            Logger.logging(Logger.Level.info,"Saved "+this.name+" to "+fileName,this);
        }


    }

    public void applyRules(TreeMap<String, String> rules) {
        applyRulesRecursively(this.minkwitzChain,rules);
    }


    /*****************************
     **** private methods  *******
     *****************************/
    private String buildToString(MinkwitzChain chain, int depth){
        String out="";
        String indent="";
        for (int i=0;i<depth;i++)
            indent+="\t";

        out+=indent;
        out+="orbit: ["+chain.getOrbit().stream().map(Object::toString).collect(Collectors.joining(","))+"]\n";
        out+=indent;
        out+="coset representative: ["+chain.getCosetRepresentatives().entrySet().stream().map(Object::toString).collect(Collectors.joining(","))+"].\n";

        if (!chain.isLast()){
            out+=indent;
            out+="stabilizer:\n";
            out+=indent;
            out+="-----------\n"+buildToString(chain.getMinkwitzChain(),depth+1);
        }
        return out;
    }

    private int[] countWordsLetters(MinkwitzChain minkwitzChain) {
        int letters = 0;
        int words = 0;

        for (GroupElement value : minkwitzChain.getCosetRepresentatives().values()) {
            if (value != null) {

                letters += value.getWord().length();
                words ++;
            }
        }
        System.out.println("Words: "+words+" Letters: "+letters);

        if (!minkwitzChain.isLast()) {
            int [] data = countWordsLetters(minkwitzChain.getMinkwitzChain());
            words += data[0];
            letters += data[1];
        }

        return new int[]{words, letters};
    }

    private String generateXMLString(MinkwitzChain minkwitzChain) {
        return this.generateXMLString(minkwitzChain,1);
    }

    private String generateXMLString(MinkwitzChain minkwitzChain,int depth) {
        StringBuilder output = new StringBuilder();
        output.append(tabs(depth-1)).append(startTag(Tag.stabilizer.name()));
        output.append(tabs(depth)).append(startTag(Tag.orbit.name()));
        output.append(tabs(depth+1)).append("[").append(minkwitzChain.getOrbit().stream().map(Object::toString).collect(Collectors.joining(","))).append("]\n");
        output.append(tabs(depth)).append(endTag(Tag.orbit.name()));
        output.append(tabs(depth)).append(startTag(Tag.representatives.name()));
        for (Map.Entry<Byte, GroupElement> entry : minkwitzChain.getCosetRepresentatives().entrySet()) {
            output.append(tabs(depth+1)).append(startTag(Tag.representative.name()," of='"+entry.getKey()+"'"));
            output.append(tabs(depth+2)).append(startTag(Tag.permutation.name()));
            if (entry.getValue() != null)
                output.append(tabs(depth+3)).append(entry.getValue().getPermutation().toString()).append("\n");
            output.append(tabs(depth+2)).append(endTag(Tag.permutation.name()));
            output.append(tabs(depth+2)).append(startTag(Tag.word.name()));
            if (entry.getValue() != null)
                output.append(tabs(depth+3)).append(entry.getValue().getWord()).append("\n");
            output.append(tabs(depth+2)).append(endTag(Tag.word.name()));
            output.append(tabs(depth+1)).append(endTag(Tag.representative.name()));
        }
        output.append(tabs(depth)).append(endTag(Tag.representatives.name()));
        if (!minkwitzChain.isLast()) output.append(generateXMLString(minkwitzChain.getMinkwitzChain(),depth+1));
        output.append(tabs(depth-1)).append(endTag(Tag.stabilizer.name()));
        return output.toString();
    }

    private void applyRulesRecursively(MinkwitzChain minkwitzChain, TreeMap<String, String> rules) {

        for (GroupElement element : minkwitzChain.getCosetRepresentatives().values())
            element.apply(rules);

    }

    /*****************************
     **** overrides     **********
     *****************************/

    public String toString(){
        String out="Stabilizer chain: \n";
        out+="=================\n";
        out+=buildToString(this,0);
        return out;

    }

    /***************************
     ********* Statics *********
     ***************************/

    public static MinkwitzChain load(String name){

        URL dirURL = IOUtils.getResourcePath("minkwitz");
        String fileName = dirURL.getFile()+"/"+name+".xml";
        try{
            SAXParserFactory spf = SAXParserFactory.newInstance();
            spf.setNamespaceAware(true);
            SAXParser saxParser = spf.newSAXParser();

            XMLReader xmlReader = saxParser.getXMLReader();

            MinkwitzParser minkwitzParser = new MinkwitzParser(name);
            xmlReader.setContentHandler(minkwitzParser);
            xmlReader.setErrorHandler(new ErrorHandler(System.err));
            xmlReader.parse(fileName);
            System.out.println("success!");

            return minkwitzParser.getMinkwitzChain();
        }
        catch(Exception ex){
            ex.getStackTrace();
            Logger.logging(Logger.Level.info,ex.getMessage()+"\n");
        }
        return null;

    }


}
