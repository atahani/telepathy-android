package com.atahani.telepathy.model;

/**
 * the class used for any operation like terminate app, that response like this {type:'TERMINATED_SUCCESSFULLY',description:'terminated successfully'}
 */
public class TOperationResultModel {
    public static String TERMINATED_SUCCESSFULLY="TERMINATED_SUCCESSFULLY";
    public String type;
    public String description;
}
