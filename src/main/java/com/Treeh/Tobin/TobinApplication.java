package com.Treeh.Tobin;

import com.Treeh.Tobin.Bed.BedIndex;
import com.Treeh.Tobin.utils.MConfig;
import htsjdk.samtools.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.*;

import static com.Treeh.Tobin.Util.fileToByte;
import static com.Treeh.Tobin.Util.makefinaldata;


@SpringBootApplication
@Controller
public class TobinApplication {
    static public ImportCluster cluster;
    static public BedIndex index;
    static public LinkedList<String> samplename;
	public static void main(String[] args) {
        cluster = new ImportCluster(MConfig.prefix +"result.txt");
//        cluster.loadData();
        index = new BedIndex();
        index.readIndex(MConfig.prefix +"out2.txt.bdx");
        File file = new File(MConfig.prefix +"data");
        samplename = new LinkedList<>();
        File[] files = file.listFiles();
        for(File f : files){
            if(f.toString().endsWith(".bam")){
                samplename.add(f.getName());
            }
        }
//        reads2sample = new ImportReads();
//        reads2sample.loadData("D:\\program\\data\\4\\data\\sample");
		SpringApplication.run(TobinApplication.class, args);
	}










}
