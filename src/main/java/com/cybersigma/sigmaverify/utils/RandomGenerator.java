package com.cybersigma.sigmaverify.utils;

import java.util.Random;

public class RandomGenerator {

    public static int randomNumber(int min, int max ){
        Random rand = new Random();
        return rand.nextInt(max - min + 1) + min;
    }
    public static int otpGenerator(){
        Random rand = new Random();
        int max=999999,min=100000;
        return rand.nextInt(max - min + 1) + min;
    }

    public static int randomNumber(int num){
        Random rand = new Random();
        return rand.nextInt(num);
    }



}
