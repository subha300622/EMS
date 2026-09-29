package com.example.ems.attendance.entity;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class AttendanceStatusConverterTest {

    private final AttendanceStatusConverter converter = new AttendanceStatusConverter();

    @Test
    public void testConvertToDatabaseColumn() {
        assertEquals("PRESENT", converter.convertToDatabaseColumn(AttendanceStatus.PRESENT));
        assertEquals("ABSENT", converter.convertToDatabaseColumn(AttendanceStatus.ABSENT));
        assertNull(converter.convertToDatabaseColumn(null));
    }

    @Test
    public void testConvertToEntityAttribute() {
        assertEquals(AttendanceStatus.PRESENT, converter.convertToEntityAttribute("Present"));
        assertEquals(AttendanceStatus.PRESENT, converter.convertToEntityAttribute("PRESENT"));
        assertEquals(AttendanceStatus.PRESENT, converter.convertToEntityAttribute("present"));
        
        assertEquals(AttendanceStatus.ABSENT, converter.convertToEntityAttribute("Absent"));
        assertEquals(AttendanceStatus.ABSENT, converter.convertToEntityAttribute("ABSENT"));
        
        assertEquals(AttendanceStatus.LEAVE, converter.convertToEntityAttribute("Leave"));
        assertEquals(AttendanceStatus.LEAVE, converter.convertToEntityAttribute("ON LEAVE"));
        
        assertEquals(AttendanceStatus.HALF_DAY, converter.convertToEntityAttribute("Half Day"));
        assertEquals(AttendanceStatus.HALF_DAY, converter.convertToEntityAttribute("HALF_DAY"));
        
        assertEquals(AttendanceStatus.LATE, converter.convertToEntityAttribute("Late"));
        
        assertNull(converter.convertToEntityAttribute(null));
        // fallback
        assertEquals(AttendanceStatus.PRESENT, converter.convertToEntityAttribute("UnknownStatus"));
    }
}
