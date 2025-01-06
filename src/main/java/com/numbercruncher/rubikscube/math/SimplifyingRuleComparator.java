package com.numbercruncher.rubikscube.math;

import java.util.Comparator;

/**
 * The class
 *
 * @author NumberCruncher
 * Since 1/3/25
 * @version 1/3/25
 */

public class SimplifyingRuleComparator implements Comparator<String> {
    @Override
    public int compare(String o1, String o2) {
        if (o1.length() != o2.length()) return o2.length()-o1.length();
        else return o1.compareTo(o2);
    }

    @Override
    public boolean equals(Object obj) {
        // Check for reference equality
        if (this == obj) {
            return true;
        }
        // Check if the object is null or not the same class
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        // Since SimplifyingRuleComparator has no additional attributes,
        // all instances of this class are inherently "equal".
        return true;
    }

}
