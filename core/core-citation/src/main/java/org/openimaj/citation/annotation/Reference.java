package org.openimaj.citation.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for bibtex-style references inside the code.
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Target(value = { ElementType.METHOD, ElementType.TYPE })
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
    int year();

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
    int[] pages() default 0;

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
    String[] editor() default "";
    
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
    int number() default -999;
    
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
    int volume() default -999;
    
    /**
     * A list of custom key value data pairs.
     * 
     * @return A list of custom key value data pairs.
     */
    String [] customData() default "";
}
