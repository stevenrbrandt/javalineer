/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.lsu.cct.javalineer;

/**
 *
 * @author sbrandt
 */
public class GuardTaskPair {
    final GuardTask gtask;
    final int index;
    public GuardTaskPair(GuardTask gt, int index) {
        gtask = gt;
        this.index = index;
    }
}
