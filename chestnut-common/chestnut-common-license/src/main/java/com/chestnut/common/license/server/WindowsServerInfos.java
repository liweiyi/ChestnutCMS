package com.chestnut.common.license.server;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * <p>用于获取客户Windows服务器的基本信息</p>
 */
@Slf4j
public class WindowsServerInfos extends AServerInfos {

    private final String CPU_COMMAND = "wmic cpu get processorid";
    private final String MAIN_BOARD_COMMAND = "wmic baseboard get serialnumber";

    @Override
    protected String getCPUSerial() throws Exception {
        StringBuilder result = new StringBuilder();
        File file = File.createTempFile("tmp", ".vbs");
        file.deleteOnExit();
        try (FileWriter fw = new FileWriter(file)) {
            String vbs = "Set objWMIService = GetObject(\"winmgmts:\\\\.\\root\\cimv2\")\n"
                    + "Set colItems = objWMIService.ExecQuery _ \n" + "   (\"Select * from Win32_Processor\") \n"
                    + "For Each objItem in colItems \n" + "    Wscript.Echo objItem.ProcessorId \n"
                    + "    exit for  ' do the first cpu only! \n" + "Next \n";
            fw.write(vbs);
        }
        Process p = Runtime.getRuntime().exec("cscript //NoLogo " + file.getPath());
        try (BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
            String line;
            while ((line = input.readLine()) != null) {
                result.append(line);
            }
        }
        Files.deleteIfExists(Path.of(file.getAbsolutePath()));
        return result.toString().trim();
    }

    @Override
    protected String getMainBoardSerial() throws Exception {
        StringBuilder result = new StringBuilder();
        File file = File.createTempFile("realhowto", ".vbs");
        file.deleteOnExit();

        try (FileWriter fw = new FileWriter(file)) {
            String vbs = "Set objWMIService = GetObject(\"winmgmts:\\\\.\\root\\cimv2\")\n"
                    + "Set colItems = objWMIService.ExecQuery _ \n" + "   (\"Select * from Win32_BaseBoard\") \n"
                    + "For Each objItem in colItems \n" + "    Wscript.Echo objItem.SerialNumber \n"
                    + "    exit for  ' do the first cpu only! \n" + "Next \n";

            fw.write(vbs);
        }
        Process p = Runtime.getRuntime().exec("cscript //NoLogo " + file.getPath());
        try (BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
            String line;
            while ((line = input.readLine()) != null) {
                result.append(line);
            }
        }
        Files.deleteIfExists(Path.of(file.getAbsolutePath()));
        return result.toString().trim();
    }
}
