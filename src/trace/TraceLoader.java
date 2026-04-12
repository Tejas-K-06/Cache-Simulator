package trace;

import exception.InvalidAddressException;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Loads memory access traces from a text file and converts them
 * into a list of MemoryAccess objects for the simulation engine.
 *
 * Expected trace file format (one access per line):
 *   R 0x0000A4F0    (read from address 0x0000A4F0)
 *   W 0x00003C10    (write to address 0x00003C10)
 *
 * Lines starting with '#' are treated as comments and ignored.
 * Blank lines are also ignored.
 *
 * Throws InvalidAddressException if a line is malformed or contains
 * an unparseable address.
 */
public class TraceLoader {

    /**
     * Parses a trace file and returns the list of memory accesses.
     *
     * Uses BufferedReader for efficient line-by-line I/O.
     *
     * @param filePath Path to the trace file (relative or absolute)
     * @return List of MemoryAccess objects in the order they appear in the file
     * @throws InvalidAddressException If a line contains a malformed address
     * @throws IOException             If the file cannot be read
     */
    public static List<MemoryAccess> load(String filePath)
            throws InvalidAddressException, IOException {

        List<MemoryAccess> accesses = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            int lineNumber = 0;

            while ((line = reader.readLine()) != null) {
                lineNumber++;

                // Trim whitespace
                line = line.trim();

                // Skip blank lines and comment lines
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                // Parse the line into operation type and address
                MemoryAccess access = parseLine(line, lineNumber);
                accesses.add(access);
            }
        }

        System.out.printf("[TraceLoader] Loaded %d memory accesses from '%s'%n",
                accesses.size(), filePath);

        return accesses;
    }

    /**
     * Parses a single line of the trace file.
     *
     * Expected format: "R 0xADDRESS" or "W 0xADDRESS"
     *
     * @param line       The raw line from the file
     * @param lineNumber The 1-based line number (for error reporting)
     * @return A MemoryAccess object representing the parsed access
     * @throws InvalidAddressException If the line format is invalid
     */
    private static MemoryAccess parseLine(String line, int lineNumber)
            throws InvalidAddressException {

        // Split on whitespace — expect exactly 2 tokens
        String[] parts = line.split("\\s+");

        if (parts.length != 2) {
            throw new InvalidAddressException(
                "Invalid trace format: expected 'R|W 0xADDRESS'",
                line, lineNumber
            );
        }

        // Parse operation type
        String opStr = parts[0].toUpperCase();
        boolean isWrite;

        if (opStr.equals("R")) {
            isWrite = false;
        } else if (opStr.equals("W")) {
            isWrite = true;
        } else {
            throw new InvalidAddressException(
                "Invalid operation type: expected 'R' or 'W', got '" + parts[0] + "'",
                parts[0], lineNumber
            );
        }

        // Parse hex address
        String addressStr = parts[1];
        int address;

        try {
            // Support both "0x" prefix and plain hex
            if (addressStr.toLowerCase().startsWith("0x")) {
                addressStr = addressStr.substring(2);
            }
            // Parse as unsigned 32-bit (use Long to handle addresses > 0x7FFFFFFF)
            long parsed = Long.parseLong(addressStr, 16);

            if (parsed < 0 || parsed > 0xFFFFFFFFL) {
                throw new InvalidAddressException(
                    "Address out of 32-bit range",
                    parts[1], lineNumber
                );
            }

            address = (int) parsed;
        } catch (NumberFormatException e) {
            throw new InvalidAddressException(
                "Cannot parse hex address: '" + parts[1] + "'",
                parts[1], lineNumber
            );
        }

        return new MemoryAccess(address, isWrite);
    }
}
