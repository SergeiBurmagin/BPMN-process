package ru.starfish24.mascotte.utils;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.variable.value.DateValue;
import org.camunda.bpm.engine.variable.value.IntegerValue;
import org.camunda.bpm.engine.variable.value.LongValue;
import org.camunda.bpm.engine.variable.value.StringValue;

import java.util.Date;

public class VariableUtils {


    public static void incInteger(final String var, final DelegateExecution execution) {

        if (execution.getVariable(var) != null) {
            IntegerValue val = execution.getVariableTyped(var);
            int integer = val.getValue();
            execution.setVariable(var, ++integer);
        }
    }

    public static Integer getIntOrThrow(final String var, final DelegateExecution execution) {
        IntegerValue val = execution.getVariableTyped(var);

        if (val == null)
            throw new ProcessVarNotFountException("Returned empty value for variable [" + var + "]");

        return val.getValue();
    }

    public static Long getLongOrThrow(final String var, final DelegateExecution execution) {
        LongValue val = execution.getVariableTyped(var);

        if (val == null) {
            throw new ProcessVarNotFountException(String.format("Returned empty value for variable [%s]", var));
        }

        return val.getValue();
    }

    public static Long getLocalLong(final String var, final DelegateExecution execution) {
        LongValue val = execution.getVariableLocalTyped(var);

        if (val == null) {
            throw new ProcessVarNotFountException(String.format("Returned empty value for variable [%s]", var));
        }

        return val.getValue();
    }

    public static Date getDateValue(final String var, final DelegateExecution execution) {
        DateValue val = execution.getVariableTyped(var);

        if (val == null) {
            throw new ProcessVarNotFountException(String.format("Returned empty value for variable [%s]", var));
        }

        return val.getValue();
    }

    public static Date getLocalDateValue(final String var, final DelegateExecution execution) {
        DateValue val = execution.getVariableLocalTyped(var);

        if (val == null) {
            throw new ProcessVarNotFountException(String.format("Returned empty value for variable [%s]", var));
        }

        return val.getValue();
    }

    public static String getStringValue(final String var, final DelegateExecution execution) {
        StringValue result = execution.getVariableTyped(var);
        return result != null ? result.getValue() : null;
    }

    public static String getLocalStringValue(final String var, final DelegateExecution execution) {
        return execution.getVariableLocal(var) != null ? execution.getVariableLocal(var).toString() : null;
    }
}
