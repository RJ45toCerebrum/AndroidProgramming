package com.example.tylerheers.humananimalyearconverter;

/**
 * Created by tylerheers on 2/12/17.
 */

public class Ratio
{
    private double num1;
    private double num2;

    public Ratio(double n1, double n2)
    {
        setRatio(n1, n2);
    }

    public void setRatio(double n1, double n2)
    {
        if(numbersValid(n1, n2)){
            num1 = (n1 <= n2) ? n1 : n2;
            num2 = (num1 == n1) ? n2 : n1;
        }
    }

    public double getNum1(){
        return num1;
    }

    public double getNum2(){
        return num2;
    }

    private boolean numbersValid(double n1, double n2)
    {
        if(n1 > 0 && n2 > 0){
            return true;
        }

        return false;
    }

    public Ratio getNormalize(){
        double scaler = 1 / num1;
        return new Ratio(num1 * scaler, num2 * scaler);
    }

    @Override
    public String toString(){
        return (new Double(num1)).toString() + ":" + (new Double(num2)).toString();
    }
}
