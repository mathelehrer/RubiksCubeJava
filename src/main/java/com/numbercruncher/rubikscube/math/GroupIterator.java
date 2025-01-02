package com.numbercruncher.rubikscube.math;

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
        GroupElement one = new GroupElement(first.multiply(first.inverse()), "");

        elements.add(this.base);
        queue.offer(one);

    }


    /*****************************
     **** public methods *********
     *****************************/
    public Stream<GroupElement> toStream(){
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(this,Spliterator.ORDERED),false);
    }



    /*****************************
     **** Overrides     **********
     *****************************/

    @Override
    public boolean hasNext() {
        return !queue.isEmpty();
    }

    @Override
    public GroupElement next() {
        if (!hasNext()) throw new IllegalStateException("No more elements");
        GroupElement element=queue.poll();

        //if the queue once exceeds the maximum number of elements we can stop generating new elements
        if (queue.size()>maxElements) limitReached=true;

        if (maxElements == -1 || !limitReached)
            //make sure that the queue is extended with every possible child of the element that is extracted from the queue
            for (GroupElement generator : generators) {
                assert element != null;
                GroupElement next = element.multiply(generator);
                Base nextBase = base.action(next.getPermutation());

                //only queue new elements when necessary

                    if (!elements.contains(nextBase)) {
                        queue.offer(next);
                        elements.add(nextBase);
                    }

                if (elements.size()%100==0)
                    System.out.println(elements.size());

            }

        return element;
    }


    /*****************************
     **** static methods **********
     *****************************/
}
