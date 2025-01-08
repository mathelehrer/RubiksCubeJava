package com.numbercruncher.rubikscube.math;

import com.numbercruncher.rubikscube.logger.Logger;
import com.numbercruncher.rubikscube.utils.IOUtils;

import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * The class GroupIterator
 *
 * @author NumberCruncher
 * Since 1/1/25
 * @version 1/1/25
 */

public class GroupIterator implements Iterator<GroupElement> {

    /*****************************
     **** Attributes **************
     *****************************/
    private final List<GroupElement> generators;
    private final Base base;
    private final HashSet<Base> elements;
    private final Deque<GroupElement> queue;
    private final int maxElements;
    private boolean limitReached = false;

    private PermutationGroup group;
    private String groupName;
    private Queue<String> shortestWords;
    private boolean loaded = false;
    private TreeMap<Character,GroupElement> generatorMap;
    private GroupElement one;
    private BufferedOutputStream out;
    /*****************************
     **** Constructor *************
     *****************************/

    public GroupIterator(List<GroupElement> generators, Base base) {
        this(generators, base, -1);
    }

    public GroupIterator(List<GroupElement> generators, Base base, int maxElements){
        this.generators=generators;
        this.base=base;
        this.maxElements = maxElements;

        elements=new HashSet<>();
        queue=new ArrayDeque<>();

        Permutation first = this.generators.get(0).getPermutation();
        one = new GroupElement(first.multiply(first.inverse()), "");

        elements.add(this.base);
        queue.offer(one);

        //create letter generator map
        generatorMap = new TreeMap<>();
        for (GroupElement generator : generators) {
            generatorMap.put(generator.getWord().charAt(0),generator);
        }


    }

    public GroupIterator(PermutationGroup group, int maxElements){
        this(group.getGroupElementGenerators(),group.getBase(),maxElements);
        this.groupName=group.getName();
        boolean success = load_from_file(maxElements);
        if (!success)
            System.out.println("Error loading group from file");
        else {
            loaded = true;
        }

    }


    /*****************************
     **** public methods *********
     *****************************/
    public Stream<GroupElement> toStream(){
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(this,Spliterator.ORDERED),false);
    }


    /*****************************
     **** private methods *********
     *****************************/

    private boolean load_from_file(int maxElements) {
        URL dirURL = IOUtils.getResourcePath("shortest_words");
        String fileName = dirURL.getFile()+"/"+this.groupName+"_"+maxElements+".txt";

        try (BufferedReader reader = new BufferedReader(new FileReader(fileName)))
        {
            String line;
            this.shortestWords = new ArrayDeque<>();

            while ((line = reader.readLine()) != null) {
                if (!line.isEmpty()) {
                    this.shortestWords.add(line);
                }
            }
            this.loaded = true;
            return true;
        }
        catch(IOException ex){
            ex.getStackTrace();
            Logger.logging(Logger.Level.warning,ex.getMessage()+"\n");
        }
        return false;
    }


    /*****************************
     **** Overrides     **********
     *****************************/

    @Override
    public boolean hasNext() {
        if (!loaded)
            return !queue.isEmpty();
        else
            return !shortestWords.isEmpty();
    }

    @Override
    public GroupElement next() {
        if (!hasNext()) throw new IllegalStateException("No more elements");

        if (!loaded) {

            if (out == null) {
                URL dirURL = IOUtils.getResourcePath("shortest_words");
                String fileName = dirURL.getFile()+"/"+this.groupName+"_"+maxElements+".txt";
                try{
                    out=new BufferedOutputStream(new FileOutputStream(fileName));
                } catch (FileNotFoundException e) {
                    Logger.logging(Logger.Level.warning,"Error, couldn't open file to save generated words: "+
                            e.getMessage()+"\n");
                }
            }



            GroupElement element = queue.poll();

            //if the queue once exceeds the maximum number of elements we can stop generating new elements
            if (elements.size() > maxElements)
                limitReached = true;

            if (maxElements == -1 || !limitReached)
                //make sure that the queue is extended with every possible child of the element that is extracted from the queue
                for (GroupElement generator : generators) {
                    assert element != null;
                    GroupElement next = element.multiply(generator);
                    Base nextBase = base.action(next.getPermutation());

                    //only queue new elements when necessary

                    if (!elements.contains(nextBase) && !limitReached) {
                        queue.offer(next);
                        elements.add(nextBase);
                        try {
                            out.write((next.toFullWordString()+"\n").getBytes());
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }

                    if (elements.size() > maxElements)
                        limitReached = true;

                }

            return element;
        }
        else{
            String word = shortestWords.poll();
            GroupElement element = one;
            assert word != null;
            for (Character c : word.toCharArray()) {
                GroupElement generator = generatorMap.get(c);
                if (generator != null) {
                    element = element.multiply(generator);
                }
            }

            return element;
        }
    }


    /*****************************
     **** static methods **********
     *****************************/
}
