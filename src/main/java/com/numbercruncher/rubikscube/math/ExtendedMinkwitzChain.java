package com.numbercruncher.rubikscube.math;

import com.numbercruncher.rubikscube.logger.Logger;
import com.numbercruncher.rubikscube.utils.IOUtils;
import com.numbercruncher.rubikscube.xml.ErrorHandler;
import com.numbercruncher.rubikscube.xml.ExtendedMinkwitzParser;
import com.numbercruncher.rubikscube.xml.MinkwitzParser;
import org.xml.sax.XMLReader;

import javax.swing.*;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import static com.numbercruncher.rubikscube.utils.StringUtils.*;

/**
 * The class StabilizerChain
 *
 * @author NumberCruncher
 * @since 2024-12-30
 * @version 2024-12-30
 */
public class ExtendedMinkwitzChain {

    /*****************************
     **** Attribute **************
     *****************************/

    public static final String XML_HEADER="<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
    public static enum Tag{stabilizer, orbit, representatives, representative,permutation, word};


    private final List<Byte> orbit;
    private final List<GroupElement> groupGenerators;
    private final Map<Byte,TreeSet<GroupElement>> cosetRepresentativesMap;
    private ExtendedMinkwitzChain stabilizerChain;
    private final String name;

    /*****************************
     **** Konstruktor*************
     *****************************/


    /**
     * The extended Minkwitz chain contains multiple representatives for each coset. Additionally, it stores the shortest word generators for each stabilizer group
     * It will be tailored to the Rubik's cube group.
     *
     * @param chain the StabilizerChain object used to initialize the MinkwitzChain's fields.
     *              Must provide valid `orbit` and optionally a `stabilizer`.
     */
    public ExtendedMinkwitzChain(StabilizerChain chain, List<GroupElement> generators, String name) {
        this.name = name;
        GroupElement one = new GroupElement(generators.get(0).multiply(generators.get(0).inverse()).getPermutation(),"");
        this.orbit = chain.getOrbit();
        this.cosetRepresentativesMap = new HashMap<>();
        this.groupGenerators = new ArrayList<>();

        if (!this.orbit.isEmpty()) {
            for (Byte b : this.orbit) {
                this.cosetRepresentativesMap.put(b,null);
            }
            TreeSet<GroupElement> cosetList = new TreeSet<>();
            cosetList.add(one);
            this.cosetRepresentativesMap.put(this.orbit.get(0), cosetList);
            this.stabilizerChain = new ExtendedMinkwitzChain(chain.getStabilizer(), generators);
        }

    }

    public ExtendedMinkwitzChain(StabilizerChain chain, List<GroupElement> generators) {
        this(chain,generators,"UnknownMinkwitzChain");
    }


    /**
     * Trained MinkwitzChain loaded from XML
     * @param orbit
     * @param cosetRepresentativesMap
     * @param stabilizer
     */
    public ExtendedMinkwitzChain(List<Byte> orbit, List<GroupElement> groupGenerators, Map<Byte,TreeSet<GroupElement>> cosetRepresentativesMap, ExtendedMinkwitzChain stabilizer, String name){
        this.name = name;
        this.orbit = orbit;
        this.cosetRepresentativesMap = cosetRepresentativesMap;
        this.stabilizerChain =stabilizer;
        this.groupGenerators = groupGenerators;
    }


    /*****************************
     **** Getter    **************
     *****************************/


    public List<Byte> getOrbit() {
        return orbit;
    }

    public TreeSet<GroupElement> getCosetRepresentatives(Byte point){
        return cosetRepresentativesMap.get(point);
    }

    public ExtendedMinkwitzChain getStabilizerChain(){
        return stabilizerChain;
    }

    public Map<Byte,TreeSet<GroupElement>> getCosetRepresentativesMap(){
        return cosetRepresentativesMap;
    }

    public int getNumberOfMissingElements(){
        int count=0;
        for (TreeSet<GroupElement> value : cosetRepresentativesMap.values()) {
            if (value ==null || value.isEmpty()) {
                count++;
            }
        }
        if (isLast())
            return count;

        else
            return count + this.stabilizerChain.getNumberOfMissingElements();
    }


    public double getAverageWordLength(){

        int[] data =  countWordsLetters(this);
        return (double) data[1]/data[0];

    }

    /*****************************
     **** Setter    **************
     *****************************/


    /**
     * This method tries to add a representative into the Minkwitz chain
     *
     * @param point
     * @param permutation
     *
     *
     */
    public void addCosetRepresentative(Byte point,GroupElement permutation,int depth){
        TreeSet<GroupElement> cosetRepresentatives = cosetRepresentativesMap.get(point);
        if (cosetRepresentatives == null) {
            cosetRepresentatives = new TreeSet<>();
        }
        if (cosetRepresentatives.isEmpty()) {
            cosetRepresentatives.add(permutation);
            this.cosetRepresentativesMap.put(point, cosetRepresentatives);
        }else{
            GroupElement rep = cosetRepresentatives.first();
            if (rep.getWord().length()>permutation.getWord().length()) {
                //System.out.println(rep.getWord()+"->"+permutation.getWord());
                cosetRepresentatives.clear();
                cosetRepresentatives.add(permutation);
            }
            else if (rep.getWord().length()==permutation.getWord().length()) {
                cosetRepresentatives.add(permutation);
                StringBuilder out= new StringBuilder();
                for (int i = 0; i < depth; i++) {
                    out.append("\t");
                }
                System.out.println(out.toString()+point+": now list length: "+cosetRepresentatives.size());
            }
        }
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
        return this.cosetRepresentativesMap.size()==this.orbit.size();
    }

    public void save(){
        this.save("");
    }

    public void save(String params){
        URL dirURL = IOUtils.getResourcePath("extended_minkwitz");
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
        applyRulesRecursively(this.stabilizerChain,rules);
    }


    /*****************************
     **** private methods  *******
     *****************************/
    private String buildToString(ExtendedMinkwitzChain chain, int depth){
        String out="";
        String indent="";
        for (int i=0;i<depth;i++)
            indent+="\t";

        out+=indent;
        out+="orbit: ["+chain.getOrbit().stream().map(Object::toString).collect(Collectors.joining(","))+"]\n";
        out+=indent;
        out+="coset representative: ["+chain.getCosetRepresentativesMap().entrySet().stream().map(Object::toString).collect(Collectors.joining(","))+"].\n";

        if (!chain.isLast()){
            out+=indent;
            out+="stabilizer:\n";
            out+=indent;
            out+="-----------\n"+buildToString(chain.getStabilizerChain(),depth+1);
        }
        return out;
    }

    private int[] countWordsLetters(ExtendedMinkwitzChain minkwitzChain) {
        int letters = 0;
        int words = 0;

        for (TreeSet<GroupElement> list : minkwitzChain.getCosetRepresentativesMap().values()) {
            if (list != null && !list.isEmpty()) {

                letters += list.first().getWord().length();
                words ++;
            }
        }
        System.out.println("Words: "+words+" Letters: "+letters);

        if (!minkwitzChain.isLast()) {
            int [] data = countWordsLetters(minkwitzChain.getStabilizerChain());
            words += data[0];
            letters += data[1];
        }

        return new int[]{words, letters};
    }

    private String generateXMLString(ExtendedMinkwitzChain minkwitzChain) {
        return this.generateXMLString(minkwitzChain,1);
    }

    private String generateXMLString(ExtendedMinkwitzChain minkwitzChain, int depth) {
        StringBuilder output = new StringBuilder();
        output.append(tabs(depth-1)).append(startTag(Tag.stabilizer.name()));
        output.append(tabs(depth)).append(startTag(Tag.orbit.name()));
        output.append(tabs(depth+1)).append("[").append(minkwitzChain.getOrbit().stream().map(Object::toString).collect(Collectors.joining(","))).append("]\n");
        output.append(tabs(depth)).append(endTag(Tag.orbit.name()));
        output.append(tabs(depth)).append(startTag(Tag.representatives.name()));
        for (Map.Entry<Byte, TreeSet<GroupElement>> entry : minkwitzChain.getCosetRepresentativesMap().entrySet()) {
            output.append(tabs(depth+1)).append(startTag(Tag.representative.name()," of='"+entry.getKey()+"'"));
            if (entry.getValue() != null)
                for (GroupElement groupElement : entry.getValue()) {
                    output.append(tabs(depth+2)).append(startTag(Tag.permutation.name()));
                    if (entry.getValue() != null)
                        output.append(tabs(depth+3)).append(groupElement.getPermutation().toString()).append("\n");
                    output.append(tabs(depth+2)).append(endTag(Tag.permutation.name()));
                    output.append(tabs(depth+2)).append(startTag(Tag.word.name()));
                    if (entry.getValue() != null)
                        output.append(tabs(depth+3)).append(groupElement.getWord()).append("\n");
                    output.append(tabs(depth+2)).append(endTag(Tag.word.name()));
                }
            output.append(tabs(depth+1)).append(endTag(Tag.representative.name()));
        }
        output.append(tabs(depth)).append(endTag(Tag.representatives.name()));
        if (!minkwitzChain.isLast()) output.append(generateXMLString(minkwitzChain.getStabilizerChain(),depth+1));
        output.append(tabs(depth-1)).append(endTag(Tag.stabilizer.name()));
        return output.toString();
    }

    private void applyRulesRecursively(ExtendedMinkwitzChain minkwitzChain, TreeMap<String, String> rules) {
        for (TreeSet<GroupElement> elements : minkwitzChain.getCosetRepresentativesMap().values())
            for (GroupElement element : elements) {
                element.apply(rules);
            }
        if (!minkwitzChain.isLast()) {
            applyRulesRecursively(minkwitzChain.getStabilizerChain(), rules);
        }
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

    public static ExtendedMinkwitzChain load(String name,int preTraining, int numberOfElements,int maxBranching){
        return load(name,preTraining,numberOfElements,maxBranching, false);
    }

    public static ExtendedMinkwitzChain load(String name,int preTraining,  int numberOfElements, int maxBranching, boolean verbose){

        URL dirURL = IOUtils.getResourcePath("extended_minkwitz");
        String fileName;
        if (maxBranching==1)
            fileName = dirURL.getFile()+"/"+name+"_"+preTraining+"_"+numberOfElements+".xml";
        else
            fileName = dirURL.getFile()+"/"+name+"_"+preTraining+"_"+numberOfElements+"_"+maxBranching+".xml";
        int i=0;
        i++;
        try{
            SAXParserFactory spf = SAXParserFactory.newInstance();
            spf.setNamespaceAware(true);
            SAXParser saxParser = spf.newSAXParser();

            XMLReader xmlReader = saxParser.getXMLReader();

            ExtendedMinkwitzParser extendedMinkwitzParser = new ExtendedMinkwitzParser(name,verbose);
            xmlReader.setContentHandler(extendedMinkwitzParser);
            xmlReader.setErrorHandler(new ErrorHandler(System.err));
            xmlReader.parse(fileName);
            if (verbose) System.out.println("success!");

            return extendedMinkwitzParser.getExtendedMinkwitzChain();
        }
        catch(Exception ex){
            ex.getStackTrace();
            Logger.logging(Logger.Level.info,ex.getMessage()+"\n");
        }
        return null;

    }


}
