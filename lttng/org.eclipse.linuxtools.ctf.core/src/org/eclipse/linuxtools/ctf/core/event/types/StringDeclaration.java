/*******************************************************************************
 * Copyright (c) 2011-2012 Ericsson, Ecole Polytechnique de Montreal and others
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Matthew Khouzam - Initial API and implementation
 * Contributors: Simon Marchi - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.ctf.core.event.types;

/**
 * <b><u>StringDeclaration</u></b>
 */
public class StringDeclaration implements IDeclaration {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    private Encoding encoding = Encoding.UTF8;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    public Encoding getEncoding() {
        return encoding;
    }

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    public StringDeclaration() {
    }

    public StringDeclaration(Encoding encoding) {
        this.encoding = encoding;
    }

    // ------------------------------------------------------------------------
    // Getters/Setters/Predicates
    // ------------------------------------------------------------------------

    public void setEncoding(Encoding encoding) {
        this.encoding = encoding;
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    @Override
    public StringDefinition createDefinition(IDefinitionScope definitionScope,
            String fieldName) {
        return new StringDefinition(this, definitionScope, fieldName);
    }

    @Override
    public String toString() {
        /* Only used for debugging */
        return "[declaration] string[" + Integer.toHexString(hashCode()) + ']'; //$NON-NLS-1$
    }

}
