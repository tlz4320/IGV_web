package com.Treeh.Tobin;

import com.Treeh.Tobin.utils.MConfig;
import htsjdk.samtools.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.*;

import static com.Treeh.Tobin.TobinApplication.cluster;
import static com.Treeh.Tobin.Util.fileToByte;
import static com.Treeh.Tobin.Util.makefinaldata;

@Controller
public class BamController {

    @RequestMapping(value = "/bam/{filename}")
    public ResponseEntity<byte[]> getBamSlice(HttpServletRequest request, @PathVariable("filename") String filename) throws Exception{
        String range = request.getHeader("Range");
        HttpHeaders headers = new HttpHeaders();
        request.getRequestURI();
        if(filename.endsWith(".bai")){
            try {
                byte[] file = fileToByte(MConfig.prefix + filename, 0, -1);
                return  new ResponseEntity<>(file, headers, HttpStatus.OK);
            }catch (Exception e){
                return  new ResponseEntity<>(null, headers, HttpStatus.NOT_ACCEPTABLE);
            }
        }
        if(range == null || range.length() < 4)
            return  new ResponseEntity<>(null, headers, HttpStatus.NOT_ACCEPTABLE);

        range = range.substring(6);
        String[] position = range.split("[-]");
        if(position.length != 2)
            return  new ResponseEntity<>(null, headers, HttpStatus.NOT_ACCEPTABLE);

        long begin = Long.parseLong(position[0]);
        long end = Long.parseLong(position[1]);
        byte[] file = fileToByte(MConfig.prefix+filename, begin, end);
        if(file == null)
            return  new ResponseEntity<>(null, headers, HttpStatus.NOT_ACCEPTABLE);
        else
            return  new ResponseEntity<>(file, headers, HttpStatus.PARTIAL_CONTENT);
    }



    @RequestMapping(value = "/bigbam/{filename}")
    public ResponseEntity<byte[]> getBigBam(HttpServletRequest request,@PathVariable("filename") String filename) throws Exception {
        String range = request.getHeader("Range");
        String chrname = request.getHeader("chr");

        HttpHeaders headers = new HttpHeaders();
        if (range == null || range.length() < 4 )
            return new ResponseEntity<>(null, headers, HttpStatus.NOT_ACCEPTABLE);
        if(chrname == null || chrname.length() == 0)
            return getBamSlice(request, filename);
        chrname = chrname.substring(3);
        range = range.substring(6);
        String[] position = range.split("[-]");
        if (position.length != 2)
            return new ResponseEntity<>(null, headers, HttpStatus.NOT_ACCEPTABLE);
        long begin = Long.parseLong(position[0]);
        long end = Long.parseLong(position[1]);

        HashMap<String, HashSet<SAMRecord>> sample2read = new HashMap<>();
        File file = new File(MConfig.prefix);
        File[] files = file.listFiles();
        int totalread = 0;
        for(File f : files) {
            if(!f.isFile() || !f.toString().endsWith(".bam"))
                continue;
            SamReader reader = SamReaderFactory.makeDefault().open(f);
            SAMRecordIterator it = reader.queryContained(chrname, (int) begin, (int) end);
            String samplename = f.getName();
            HashSet<SAMRecord> set = new HashSet<>();
            sample2read.put(samplename, set);
            while (it.hasNext()) {
                SAMRecord r = it.next();
                set.add(r);
                totalread++;
            }
            it.close();
        }
        HashMap<String, HashSet<SAMRecord>> selectreads = new HashMap();
        int maxrows = 500;
        int finalreadsnum = totalread;
        Random random = new Random();
        HashSet<SAMRecord> set;
        if (totalread > maxrows) {
            finalreadsnum = 0;
            for (Map.Entry<String, HashSet<SAMRecord>> e : sample2read.entrySet()) {
                set = e.getValue();
                int length = maxrows * set.size() / totalread + 1;
                double threshold = 1.2 * length / (double)set.size();
                HashSet<SAMRecord> newset = new HashSet<>();
                for(SAMRecord record: set){
                    if(random.nextDouble() < threshold)
                        newset.add(record);
                    if(newset.size() >= length)
                        break;
                }
                Iterator<SAMRecord> i = set.iterator();
                while(newset.size() <= length && i.hasNext())
                    newset.add(i.next());
                selectreads.put(e.getKey(), newset);
                finalreadsnum += newset.size();
            }
        }
        else
            selectreads = sample2read;
        byte[] res = makefinaldata(selectreads, finalreadsnum);
        return  new ResponseEntity<>(res, headers, HttpStatus.PARTIAL_CONTENT);
    }
}
