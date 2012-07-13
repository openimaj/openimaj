/**
 * Copyright (c) 2011, The University of Southampton and the individual contributors.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *   * 	Redistributions of source code must retain the above copyright notice,
 * 	this list of conditions and the following disclaimer.
 *
 *   *	Redistributions in binary form must reproduce the above copyright notice,
 * 	this list of conditions and the following disclaimer in the documentation
 * 	and/or other materials provided with the distribution.
 *
 *   *	Neither the name of the University of Southampton nor the names of its
 * 	contributors may be used to endorse or promote products derived from this
 * 	software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.openimaj.citation.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for bibtex-style references inside the code.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Target(value = { ElementType.METHOD, ElementType.TYPE, ElementType.PACKAGE })
public @interface Reference {

    /**
     * The name(s) of the author(s)
     * 
     * @return The name(s) of the author(s)
     */
    String[] author();

    /**
     * The title of the work
     * 
     * @return The title of the work
     */
    String title();

    /**
     * The type of publication
     * 
     * @return The type of publication
     * @see ReferenceType
     */
    ReferenceType type();

    /**
     * The year of publication (or, if unpublished, the year of creation)
     * 
     * @return The year of publication (or, if unpublished, the year of creation)
     */
    String year();

    /**
     * The journal or magazine the work was published in
     * 
     * @return The journal or magazine the work was published in
     */
    String journal()  default "";
    
    /**
     * The title of the book, if only part of it is being cited
     * 
     * @return The title of the book, if only part of it is being cited
     */
    String booktitle() default "";

    /**
     * Page numbers 
     * 
     * @return Page numbers
     */
    String[] pages() default {};

    /**
     * The chapter number
     * 
     * @return The chapter number
     */
    String chapter() default "";
    
    /**
     * The edition of a book, long form (such as "first" or "second")
     * 
     * @return  The edition of a book, long form (such as "first" or "second")
     */
    String edition() default "";
    
    /**
     * An optional URL reference where the publication can be found.
     * 
     * @return A URL where the reference can be found. 
     */
    String url() default "";

    /**
     * Miscellaneous extra information
     * 
     * @return Miscellaneous extra information
     */
    String note() default "";
    
    /**
     * The name(s) of the editor(s)
     * 
     * @return The name(s) of the editor(s)
     */
    String[] editor() default {};
    
    /**
     * The institution that was involved in the publishing, but not necessarily the publisher
     * 
     * @return The institution that was involved in the publishing, but not necessarily the publisher
     */
    String institution() default "";
    
    /**
     * The month of publication (or, if unpublished, the month of creation)
     * 
     * @return The month of publication (or, if unpublished, the month of creation)
     */
    String month() default "";
    
    /**
     * The "(issue) number" of a journal, magazine, or tech-report, if applicable. (Most publications have a "volume", but no "number" field.)
     * 
     * @return The "(issue) number" of a journal, magazine, or tech-report, if applicable. (Most publications have a "volume", but no "number" field.)
     */
    String number() default "";
    
    /**
     * The conference sponsor
     * 
     * @return The conference sponsor
     */
    String organization() default "";;
    
    /**
     * The publisher's name
     * 
     * @return The publisher's name
     */
    String publisher() default "";
    
    /**
     * The school where the thesis was written
     * 
     * @return The school where the thesis was written
     */
    String school() default "";
    
    /**
     * The series of books the book was published in (e.g. "The Hardy Boys" or "Lecture Notes in Computer Science")
     * 
     * @return The series of books the book was published in (e.g. "The Hardy Boys" or "Lecture Notes in Computer Science")
     */
    String series() default "";
    
    /**
     * The volume of a journal or multi-volume book
     * 
     * @return The volume of a journal or multi-volume book
     */
    String volume() default "";
    
    /**
     * A list of custom key value data pairs.
     * 
     * @return A list of custom key value data pairs.
     */
    String [] customData() default {};
}
