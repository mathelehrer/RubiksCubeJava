package com.numbercruncher.rubikscube.xml;

import com.numbercruncher.rubikscube.logger.Logger;
import com.numbercruncher.rubikscube.math.GroupElement;
import com.numbercruncher.rubikscube.math.MinkwitzChain;
import com.numbercruncher.rubikscube.math.Permutation;
import com.numbercruncher.rubikscube.utils.StringUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.*;
import java.util.stream.Collectors;

/**
 * The class MinkwitzParser
 *
 * @author NumberCruncher
 * Since 1/2/25
 * @version 1/2/25
 */

// Container that collect the raw data from the XML file
class StabilizerContainer{
    private byte[] orbit;
    private RepresentativesContainer representatives;
    private StabilizerContainer stabilizer;

    public void addOrbit(byte[] orbit){
        this.orbit=orbit;
    }
    public List<Byte> getOrbit(){
        List<Byte> list =new ArrayList<>();
        for (byte b : orbit)
            list.add(b);
        return list;
    }

    public void addRepresentatives(RepresentativesContainer representatives){
        this.representatives=representatives;
    }

    public void setStabilizer(StabilizerContainer stabilizer){
        this.stabilizer=stabilizer;
    }

    public StabilizerContainer getStabilizer(){
        return stabilizer;
    }

    public Map<Byte,GroupElement> getRepresentatives(){
        return this.representatives.getRepresentatives();
    }

    private static String buildString(StabilizerContainer container){
        if (container.stabilizer==null)
            return Arrays.toString(container.orbit);
        else
            return Arrays.toString(container.orbit)+"->"+buildString(container.stabilizer);
    }

    public String toString(){
        return buildString(this);
    }

}

class RepresentativesContainer{
    Map<Byte,GroupElement> representatives;

    public RepresentativesContainer() {
        representatives=new HashMap<>();
    }

    public void addRepresentative(RepresentativeContainer representative){
        representatives.put(representative.getOrbitPoint(),representative.getGroupElement());
    }

    public Map<Byte, GroupElement> getRepresentatives() {
            return representatives;
    }

    @Override
    public String toString() {
        return "RepresentativesContainer{" +
                "representatives=[" + representatives.entrySet().stream().map(v->v.getKey()+"->"+v.getValue()).collect(Collectors.joining(",")) +
                "]}";
    }
}

class RepresentativeContainer{
    Byte orbitPoint;
    Permutation permutation;
    String word;

    public RepresentativeContainer(Byte orbitPoint) {
        this.orbitPoint = orbitPoint;
    }

    public void setPermutation(Permutation permutation){
        this.permutation=permutation;
    }

    public void setWord(String word){
        this.word=word;
    }

    public GroupElement getGroupElement(){
        return new GroupElement(permutation,word);
    }

    public Byte getOrbitPoint(){
        return orbitPoint;
    }

    public String toString(){
        return "RepresentativeContainer{" +
                "orbitPoint=" + orbitPoint +
                ", permutation=" + permutation +
                ", word='" + word + '\'' +
                '}';
    }


}

public class MinkwitzParser extends DefaultHandler {

    private final Stack<String> elementStack = new Stack<String>();
    private final Stack<Object> objectStack  = new Stack<Object>();

    private MinkwitzChain chain;
    private StabilizerContainer stabilizerContainer;

    private String name;

    public MinkwitzParser(String name) {
        this.name=name;
    }

    public MinkwitzChain getMinkwitzChain(){
        buildChain();
        return this.chain;
    }

    private void buildChain(){
        this.chain = buildChain(stabilizerContainer,0);
    }

    private MinkwitzChain buildChain(StabilizerContainer container,int depth){
        String name = "";
        if (depth==0){
            name=this.name;
        }
        if (container.getStabilizer()==null)
            return new MinkwitzChain(container.getOrbit(),container.getRepresentatives(),null,name);
        else
            return new MinkwitzChain(container.getOrbit(),container.getRepresentatives(),buildChain(container.getStabilizer(),depth+1),name);
    }

    public void startDocument() throws SAXException {
        Logger.logging(Logger.Level.info,"Start reading from file!");
    }

    public void endDocument() throws SAXException {
        Logger.logging(Logger.Level.info,"Parsing completed.");
    }

    public void startElement(String namespaceURI,
                             String localName,
                             String qName,
                             Attributes atts) throws SAXException {

        this.elementStack.push(qName);

        switch (MinkwitzChain.Tag.valueOf(qName)){
            case stabilizer:
                //placeholder for stabilizer chain
                this.objectStack.push(new StabilizerContainer());
                break;
            case orbit:
                //placeholder for orbit data
                this.objectStack.push(new StringBuilder());
                break;
            case representatives:
                this.objectStack.push(new RepresentativesContainer());
                break;
            case representative:
                Byte orbitPoint = atts.getValue("of") == null ? 0 : Byte.parseByte(atts.getValue("of"));
                Logger.logging(Logger.Level.info,"Reading orbit point "+orbitPoint);
                this.objectStack.push(new RepresentativeContainer(orbitPoint));
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
                StabilizerContainer newStabilizer = (StabilizerContainer) obj;
                if (parentObj instanceof StabilizerContainer parentChain) {
                    parentChain.setStabilizer(newStabilizer);
                    Logger.logging(Logger.Level.info,"stabilizer container "+newStabilizer);
                    Logger.logging(Logger.Level.info,"added to parent "+parentChain);
                }
                else{
                    //the last container is made available inside the class
                    //buildchain() will construct the chain from it
                    this.stabilizerContainer = newStabilizer;
                    Logger.logging(Logger.Level.info,"Creating final container "+this.stabilizerContainer);
                }
                break;
            case orbit:
                byte[] orbit = StringUtils.parseByteArray(((StringBuilder) obj).toString());
                if (parentObj!=null) ((StabilizerContainer) parentObj).addOrbit(orbit);
                break;
            case representatives:
                RepresentativesContainer reps = (RepresentativesContainer) obj;
                if (parentObj!=null) ((StabilizerContainer) parentObj).addRepresentatives(reps);
                Logger.logging(Logger.Level.info,"Adding representatives "+reps);
                break;
            case representative:
                RepresentativeContainer rep = (RepresentativeContainer) obj;
                if (parentObj!=null) ((RepresentativesContainer) parentObj).addRepresentative(rep);
                Logger.logging(Logger.Level.info,"Adding representative "+rep);
                break;
            case permutation:
                Permutation perm = Permutation.parse(((StringBuilder) obj).toString());
                if (parentObj!=null) ((RepresentativeContainer) parentObj).setPermutation(perm);
                break;
            case word:
                String word = ((StringBuilder) obj).toString();
                if (parentObj!=null) ((RepresentativeContainer) parentObj).setWord(word);
                break;
        }
    }

    /**
     * The reading of the data takes place in this function
     */
    public void characters(char ch[], int start, int length) {

        String value = new String(ch, start, length).trim();
        if(value.isEmpty()) return; // ignore white space

        MinkwitzChain.Tag elementTag =MinkwitzChain.Tag.valueOf(this.currentElement());
        MinkwitzChain.Tag parentTag =MinkwitzChain.Tag.valueOf(this.currentElementParent());

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
