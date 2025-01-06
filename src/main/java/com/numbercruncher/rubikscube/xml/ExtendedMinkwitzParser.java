package com.numbercruncher.rubikscube.xml;

import com.numbercruncher.rubikscube.logger.Logger;
import com.numbercruncher.rubikscube.math.ExtendedMinkwitzChain;
import com.numbercruncher.rubikscube.math.GroupElement;
import com.numbercruncher.rubikscube.math.MinkwitzChain;
import com.numbercruncher.rubikscube.math.Permutation;
import com.numbercruncher.rubikscube.utils.StringUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.*;
import java.util.stream.Collectors;


class ExtendedStabilizerContainer{
    private byte[] orbit;
    private ExtendedRepresentativesContainer representatives;
    private ExtendedStabilizerContainer stabilizer;

    public void addOrbit(byte[] orbit){
        this.orbit=orbit;
    }
    public List<Byte> getOrbit(){
        List<Byte> list =new ArrayList<>();
        for (byte b : orbit)
            list.add(b);
        return list;
    }

    public void addRepresentatives(ExtendedRepresentativesContainer representatives){
        this.representatives=representatives;
    }

    public void setStabilizer(ExtendedStabilizerContainer stabilizer){
        this.stabilizer=stabilizer;
    }

    public ExtendedStabilizerContainer getStabilizer(){
        return stabilizer;
    }

    public Map<Byte,TreeSet<GroupElement>> getRepresentativesMap(){
        return this.representatives.getRepresentatives();
    }

    private static String buildString(ExtendedStabilizerContainer container){
        if (container.stabilizer==null)
            return Arrays.toString(container.orbit);
        else
            return Arrays.toString(container.orbit)+"->"+buildString(container.stabilizer);
    }

    public String toString(){
        return buildString(this);
    }
}

class ExtendedRepresentativesContainer{
    Map<Byte,TreeSet<GroupElement>> representatives;

    public ExtendedRepresentativesContainer() {
        representatives=new HashMap<>();
    }

    public void addRepresentatives(ExtendedRepresentativeContainer representative){
        byte orbitPoint = representative.getOrbitPoint();
        representatives.put(orbitPoint,representative.getGroupElements());
    }

    public Map<Byte, TreeSet<GroupElement>> getRepresentatives() {
        return representatives;
    }

    @Override
    public String toString() {
        return "RepresentativesContainer{" +
                "representatives=[" + representatives.entrySet().stream().map(v->v.getKey()+"->{"+v.getValue().stream().map(GroupElement::toString).collect(Collectors.joining(","))+"}").collect(Collectors.joining(",")) +
                "]}";
    }
}


class ExtendedRepresentativeContainer{
    Byte orbitPoint;
    Permutation permutation;
    TreeSet<GroupElement> groupElements;

    public ExtendedRepresentativeContainer(Byte orbitPoint) {
        this.orbitPoint = orbitPoint;
        groupElements = new TreeSet<>();
    }

    public void setPermutation(Permutation permutation){
        this.permutation=permutation;
    }

    public void setWord(String word){
        groupElements.add(new GroupElement(this.permutation,word));
    }

    public TreeSet<GroupElement> getGroupElements(){
        return this.groupElements;
    }

    public Byte getOrbitPoint(){
        return orbitPoint;
    }

    public String toString(){
        return "RepresentativeContainer{" +
                "orbitPoint=" + orbitPoint +
                ": ["+ this.groupElements.stream().map(GroupElement::toString).collect(Collectors.joining(",")) +"]}";
    }
}

public class ExtendedMinkwitzParser extends DefaultHandler {

    private final Stack<String> elementStack = new Stack<String>();
    private final Stack<Object> objectStack  = new Stack<Object>();

    private ExtendedMinkwitzChain chain;
    private ExtendedStabilizerContainer stabilizerContainer;

    private final String name;
    private boolean verbose;

    public ExtendedMinkwitzParser(String name, boolean verbose) {
        this.name=name;
        this.verbose =verbose;
    }

    public ExtendedMinkwitzChain getExtendedMinkwitzChain(){
        buildChain();
        return this.chain;
    }

    private void buildChain(){
        this.chain = buildChain(stabilizerContainer,0);
    }

    private ExtendedMinkwitzChain buildChain(ExtendedStabilizerContainer container,int depth){
        String name = "";
        if (depth==0){
            name=this.name;
        }
        if (container.getStabilizer()==null)
            return new ExtendedMinkwitzChain(container.getOrbit(),null,container.getRepresentativesMap(),null,name);
        else
            return new ExtendedMinkwitzChain(container.getOrbit(),null,container.getRepresentativesMap(),buildChain(container.getStabilizer(),depth+1),name);
    }

    public void startDocument() throws SAXException {
        if (verbose) Logger.logging(Logger.Level.info,"Start reading from file!");
    }

    public void endDocument() throws SAXException {
        if (verbose) Logger.logging(Logger.Level.info,"Parsing completed.");
    }

    public void startElement(String namespaceURI,
                             String localName,
                             String qName,
                             Attributes atts) throws SAXException {

        this.elementStack.push(qName);
        switch (MinkwitzChain.Tag.valueOf(qName)){
            case stabilizer:
                //placeholder for stabilizer chain
                this.objectStack.push(new ExtendedStabilizerContainer());
                break;
            case orbit:
                //placeholder for orbit data
                this.objectStack.push(new StringBuilder());
                break;
            case representatives:
                this.objectStack.push(new ExtendedRepresentativesContainer());
                break;
            case representative:
                Byte orbitPoint = atts.getValue("of") == null ? 0 : Byte.parseByte(atts.getValue("of"));
                if (verbose) Logger.logging(Logger.Level.info,"Reading orbit point "+orbitPoint);
                this.objectStack.push(new ExtendedRepresentativeContainer(orbitPoint));
                break;
            case permutation, word:
                this.objectStack.push(new StringBuilder());
                break;
        }
    }

    /**
     * construct objects from the given data
     */
    public void endElement(String uri, String localName,
                           String qName) throws SAXException {

        Object obj=this.objectStack.pop();
        Object parentObj=null;
        if (!this.objectStack.isEmpty()){
            parentObj=this.objectStack.peek();
        }

        switch (MinkwitzChain.Tag.valueOf(qName)) {
            case stabilizer:
                ExtendedStabilizerContainer newStabilizer = (ExtendedStabilizerContainer) obj;
                if (parentObj instanceof ExtendedStabilizerContainer parentChain) {
                    parentChain.setStabilizer(newStabilizer);
                    if (verbose) {
                        Logger.logging(Logger.Level.info, "extended stabilizer container " + newStabilizer);
                        Logger.logging(Logger.Level.info, "added to parent " + parentChain);
                    }
                }
                else{
                    //the last container is made available inside the class
                    //buildchain() will construct the chain from it
                    this.stabilizerContainer = newStabilizer;
                    if (verbose) Logger.logging(Logger.Level.info,"Creating final container "+this.stabilizerContainer);
                }
                break;
            case orbit:
                byte[] orbit = StringUtils.parseByteArray(((StringBuilder) obj).toString());
                if (parentObj!=null) ((ExtendedStabilizerContainer) parentObj).addOrbit(orbit);
                break;
            case representatives:
                ExtendedRepresentativesContainer reps = (ExtendedRepresentativesContainer) obj;
                if (parentObj!=null) ((ExtendedStabilizerContainer) parentObj).addRepresentatives(reps);
                if (verbose) Logger.logging(Logger.Level.info,"Adding extended representatives "+reps);
                break;
            case representative:
                //nothing to do here, the representative is added to the list automatically, one the word is parsed
                ExtendedRepresentativeContainer rep = (ExtendedRepresentativeContainer) obj;
                if (parentObj!=null) ((ExtendedRepresentativesContainer) parentObj).addRepresentatives(rep);
//                if (verbose) Logger.logging(Logger.Level.info,"Adding representative "+rep);
                break;
            case permutation:
                Permutation perm = Permutation.parse(((StringBuilder) obj).toString());
                if (parentObj!=null) ((ExtendedRepresentativeContainer) parentObj).setPermutation(perm);
                break;
            case word:
                String word = ((StringBuilder) obj).toString();
                if (parentObj!=null) ((ExtendedRepresentativeContainer) parentObj).setWord(word);
                break;
        }
    }

    /**
     * The reading of the data takes place in this function
     */
    public void characters(char ch[], int start, int length) {

        String value = new String(ch, start, length).trim();
        if(value.isEmpty()) return; // ignore white space

        ExtendedMinkwitzChain.Tag elementTag =ExtendedMinkwitzChain.Tag.valueOf(this.currentElement());
        ExtendedMinkwitzChain.Tag parentTag =ExtendedMinkwitzChain.Tag.valueOf(this.currentElementParent());

        switch (elementTag) {
            //only between these three tags actual data can be collected from the file
            case orbit,permutation,word:
                // Append characters to the current placeholder
                if (this.currentObject() instanceof StringBuilder sb) {
                    sb.append(value);
                }
                break;
        }
    }

    private String currentElement() {
        return this.elementStack.peek();
    }

    private String currentElementParent() {
        if(this.elementStack.size() < 2) return null;
        return this.elementStack.get(this.elementStack.size()-2);
    }

    private Object currentObject(){
        return this.objectStack.peek();
    }

    private Object currentObjectParent(){
        if (this.objectStack.size()<2) return null;
        return this.objectStack.get(this.objectStack.size()-2);
    }

}
