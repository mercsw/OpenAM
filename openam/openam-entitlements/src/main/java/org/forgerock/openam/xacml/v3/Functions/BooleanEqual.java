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
urn:oasis:names:tc:xacml:1.0:function:boolean-equal
This function SHALL take two arguments of data-type “http://www.w3.org/2001/XMLSchema#boolean”
and SHALL return an “http://www.w3.org/2001/XMLSchema#boolean”.
The function SHALL return "True" if and only if the arguments are equal.
Otherwise, it SHALL return “False”.*/

import org.forgerock.openam.xacml.v3.Entitlements.FunctionArgument;
import org.forgerock.openam.xacml.v3.Entitlements.XACMLEvalContext;

/**
 * urn:oasis:names:tc:xacml:1.0:function:boolean-equal
 */
public class BooleanEqual extends XACMLFunction {

    public BooleanEqual()  {
    }

    public FunctionArgument evaluate( XACMLEvalContext pip){
        FunctionArgument retVal =  FunctionArgument.falseObject;

        if ( getArgCount() != 2) {
            return retVal;
        }
        Object s = getArg(0).getValue(pip);
        Object ob = getArg(1).getValue(pip);

        if ( (s == null) || (ob == null) ) {
            return retVal;
        }
        boolean boolean1 = Boolean.parseBoolean((String)s);
        boolean boolean2 = Boolean.parseBoolean((String)ob);
        if ( boolean1 == boolean2) {
            retVal = FunctionArgument.trueObject;
        } else {
            retVal = FunctionArgument.falseObject;
        }
        return retVal;
    }
}
