package com.example.tylerheers.humananimalyearconverter;

import java.math.BigInteger;
import java.security.InvalidParameterException;

/**
 * Created by tylerheers on 2/12/17.
 */

public class RationalNumber
{
    private Integer numerator;
    private Integer denominator;

    public RationalNumber(Integer num, Integer denom)
    {
        setNumerator(num);
        setDenominator(denom);
    }

    public void setNumerator(Integer num){
        numerator = num;
    }

    public void setDenominator(Integer denom)
    {
        if(denom.doubleValue() == 0.0){
            throw new InvalidParameterException("Divide by Zero");
        }

        denominator = denom;
    }

    public Integer getNumerator(){
        return numerator;
    }

    public Integer getDenominator(){
        return denominator;
    }

    public RationalNumber getNormalizedRational()
    {
        Integer divisor = gcd(numerator, denominator);
        return new RationalNumber(numerator/divisor, denominator/divisor);
    }

    public Integer gcd(Integer a, Integer b)
    {
        if(b == 0){
            return a;
        }
        return gcd(b, a % b);
    }

    @Override
    public String toString(){
        return numerator.toString() + '/' + denominator.toString();
    }
}
