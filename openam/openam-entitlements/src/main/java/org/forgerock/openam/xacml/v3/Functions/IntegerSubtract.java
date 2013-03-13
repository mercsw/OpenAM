/**
 *
 ~ DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 ~
 ~ Copyright (c) 2011-2013 ForgeRock AS. All Rights Reserved
 ~
 ~ The contents of this file are subject to the terms
 ~ of the Common Development and Distribution License
 ~ (the License). You may not use this file except in
 ~ compliance with the License.
 ~
 ~ You can obtain a copy of the License at
 ~ http://forgerock.org/license/CDDLv1.0.html
 ~ See the License for the specific language governing
 ~ permission and limitations under the License.
 ~
 ~ When distributing Covered Code, include this CDDL
 ~ Header Notice in each file and include the License file
 ~ at http://forgerock.org/license/CDDLv1.0.html
 ~ If applicable, add the following below the CDDL Header,
 ~ with the fields enclosed by brackets [] replaced by
 ~ your own identifying information:
 ~ "Portions Copyrighted [year] [name of copyright owner]"
 *
 */
package org.forgerock.openam.xacml.v3.Functions;

/*
urn:oasis:names:tc:xacml:1.0:function:integer-subtract
functions SHALL take two arguments of the specified data-type, integer, or double,
and SHALL return an element of integer or double data-type, respectively.
However, the “add” and “multiply” functions MAY take more than two arguments.
Each function evaluation operating on doubles SHALL proceed as specified by their logical counterparts
 in IEEE 754 [IEEE754]. For all of these functions, if any argument is "Indeterminate",
 then the function SHALL evaluate to "Indeterminate".
 In the case of the divide functions, if the divisor is zero,
 then the function SHALL evaluate to “Indeterminate”.
*/

import org.forgerock.openam.xacml.v3.Entitlements.*;

/**
 * urn:oasis:names:tc:xacml:1.0:function:integer-subtract
 */
public class IntegerSubtract extends XACMLFunction {

    public IntegerSubtract()  {
    }

    public FunctionArgument evaluate( XACMLEvalContext pip) throws XACML3EntitlementException {
        FunctionArgument retVal = new DataValue(DataType.XACMLINTEGER, 0);
        if ( getArgCount() != 2) {
            return retVal;
        }
        Integer arg0 = getArg(0).asInteger(pip);
        Integer arg1 = getArg(1).asInteger(pip);

        retVal = new DataValue(DataType.XACMLINTEGER, (arg0 - arg1));
        // return the Deducted Value.
        return retVal;
    }
}
