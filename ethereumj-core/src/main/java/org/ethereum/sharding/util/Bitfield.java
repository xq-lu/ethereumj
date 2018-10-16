/*
 * Copyright (c) [2016] [ <ether.camp> ]
 * This file is part of the ethereumJ library.
 *
 * The ethereumJ library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The ethereumJ library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ethereumJ library. If not, see <http://www.gnu.org/licenses/>.
 */
package org.ethereum.sharding.util;

import java.util.Arrays;
import java.util.BitSet;
import java.util.List;

/**
 * Bitfield is bit array where every bit represents status of
 * attester with corresponding index
 *
 * All methods that could change payload are cloning source,
 * keeping instances of Bitfield immutable.
 */
public class Bitfield {

    private final BitSet payload;
    private final int size; // in Bits

    private Bitfield(int size) {
        this.size = calcLength(size) * Byte.SIZE;
        this.payload = new BitSet(size);
    }

    public Bitfield(byte[] data) {
        this.size = data.length * Byte.SIZE;
        this.payload = BitSet.valueOf(data);
    }

    /**
     * Calculates attesters bitfield length
     * @param num  Number of attesters
     * @return  Bitfield length in bytes
     */
    private int calcLength(int num) {
        return num == 0 ? 0 : (num - 1) / Byte.SIZE + 1;
    }

    /**
     * Creates empty bitfield for estimated number of attesters
     * @param validatorsCount   Number of attesters
     * @return  empty bitfield with correct length
     */
    public static Bitfield createEmpty(int validatorsCount) {
        return new Bitfield(validatorsCount);
    }

    /**
     * Modifies bitfield to represent attester's vote
     * Should place its bit on the right place
     * Doesn't modify original bitfield
     * @param index     Index number of attester
     * @return  bitfield with vote in place
     */
    public Bitfield markVote(int index) {
        Bitfield newBitfield = this.clone();
        newBitfield.payload.set(index);
        return newBitfield;
    }

    /**
     * Checks whether validator with provided index did his vote
     * @param index     Index number of attester
     */
    public boolean hasVoted(int index) {
        return payload.get(index);
    }

    /**
     * Calculate number of votes in provided bitfield
     * @return  number of votes
     */
    public int calcVotes() {
        int votes = 0;
        for (int i = 0; i < size(); ++i) {
            if (hasVoted(i)) ++votes;
        }

        return votes;
    }

    /**
     * OR aggregation function
     * OR aggregation of input bitfields
     * @param bitfields  Bitfields
     * @return All bitfields aggregated using OR
     */
    public static Bitfield orBitfield(List<Bitfield> bitfields) {
        if (bitfields.isEmpty()) return null;

        int bitfieldLen = bitfields.get(0).size();
        Bitfield aggBitfield = new Bitfield(bitfieldLen);
        for (int i = 0; i < bitfieldLen; ++i) {
            for (Bitfield bitfield : bitfields) {
                if (aggBitfield.payload.get(i) | bitfield.payload.get(i)) {
                    aggBitfield.payload.set(i);
                }
            }
        }

        return aggBitfield;
    }

    public int size() {
        return size;
    }

    public byte[] getData() {
        return Arrays.copyOf(payload.toByteArray(), size * Byte.SIZE);
    }

    public Bitfield clone() {
        return new Bitfield(getData());
    }

}
