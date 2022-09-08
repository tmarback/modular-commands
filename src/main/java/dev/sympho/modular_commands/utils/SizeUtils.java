package dev.sympho.modular_commands.utils;

import java.util.List;

import org.checkerframework.common.value.qual.IntRange;

/**
 * Utilities for working with file sizes.
 * 
 * <p>Note the possibility of integer overflow depending on the data type used.
 *
 * @version 1.0
 * @since 1.0
 */
public final class SizeUtils {

    /** The factor between prefixes. */
    private static final int FACTOR = 1000;

    /** The value of the K prefix. */
    public static final int KILO = FACTOR;

    /** The value of the M prefix. */
    public static final int MEGA = KILO * FACTOR;

    /** The value of the G prefix. */
    public static final int GIGA = MEGA * FACTOR;

    /** The value of the T prefix. */
    public static final long TERA = GIGA * ( long ) FACTOR;
    
    /** The supported prefixes. */
    private static final List<Prefix> PREFIXES = List.of(
        new Prefix( "T", TERA ),
        new Prefix( "G", GIGA ),
        new Prefix( "M", MEGA ),
        new Prefix( "K", KILO )
    );

    /** Do not instantiate. */
    private SizeUtils() {}

    /**
     * Makes a size in kilobytes.
     *
     * @param size The size in KB.
     * @return The size in bytes.
     */
    public static int kilo( final @IntRange( from = 0, to = Integer.MAX_VALUE / KILO ) int size ) {
        return size * KILO;
    }

    /**
     * Makes a size in kilobytes.
     *
     * @param size The size in KB.
     * @return The size in bytes.
     */
    public static long kilo( final @IntRange( from = 0, to = Long.MAX_VALUE / KILO ) long size ) {
        return size * KILO;
    }

    /**
     * Makes a size in megabytes.
     *
     * @param size The size in MB.
     * @return The size in bytes.
     */
    public static int mega( final @IntRange( from = 0, to = Integer.MAX_VALUE / MEGA ) int size ) {
        return size * MEGA;
    }

    /**
     * Makes a size in megabytes.
     *
     * @param size The size in MB.
     * @return The size in bytes.
     */
    public static long mega( final @IntRange( from = 0, to = Long.MAX_VALUE / MEGA ) long size ) {
        return size * MEGA;
    }

    /**
     * Makes a size in gigabytes.
     *
     * @param size The size in GB.
     * @return The size in bytes.
     */
    public static int giga( final @IntRange( from = 0, to = Integer.MAX_VALUE / GIGA ) int size ) {
        return size * GIGA;
    }

    /**
     * Makes a size in gigabytes.
     *
     * @param size The size in GB.
     * @return The size in bytes.
     */
    public static long giga( final @IntRange( from = 0, to = Long.MAX_VALUE / GIGA ) long size ) {
        return size * GIGA;
    }

    /**
     * Makes a size in terabytes.
     *
     * @param size The size in TB.
     * @return The size in bytes.
     */
    public static long tera( final @IntRange( from = 0, to = Long.MAX_VALUE / TERA ) long size ) {
        return size * TERA;
    }

    /**
     * Formats a size to the nearest (supported) prefix.
     *
     * @param size The size to format.
     * @return The formatted size.
     */
    public static String format( final @IntRange( from = 0 ) long size ) {

        return PREFIXES.stream()
            .dropWhile( p -> p.value() > size )
            .findFirst()
            .map( p -> p.format( size ) )
            .orElseGet( () -> "%d bytes".formatted( size ) );

    }

    /**
     * A supported prefix.
     *
     * @param prefix The prefix letter.
     * @param value The prefix value.
     * @since 1.0
     */
    private record Prefix( String prefix, long value ) {

        /**
         * Formats a size to this prefix.
         *
         * @param size The size to format.
         * @return The formatted size.
         */
        public String format( final @IntRange( from = 0 ) long size ) {

            final double s = size;
            final double v = s / value;
            return "%.2f%sB".formatted( v, prefix );

        }

    }
    
}
