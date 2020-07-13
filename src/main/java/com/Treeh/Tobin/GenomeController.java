package com.Treeh.Tobin;

import com.Treeh.Tobin.utils.MConfig;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.stereotype.Controller;
import static com.Treeh.Tobin.Util.fileToByte;
import javax.servlet.http.HttpServletRequest;


@Controller
public class GenomeController {
    @RequestMapping(value = "/genomes/{filename}")
    public ResponseEntity<byte[]> getgenomes(HttpServletRequest request, @PathVariable("filename") String filename) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        String range = request.getHeader("Range");
        if (range == null || range.length() < 4 )
            return new ResponseEntity<>(null, headers, HttpStatus.NOT_ACCEPTABLE);
        range = range.substring(6);
        String[] position = range.split("[-]");
        if (position.length != 2)
            return new ResponseEntity<>(null, headers, HttpStatus.NOT_ACCEPTABLE);
        long begin = Long.parseLong(position[0]);
        long end = Long.parseLong(position[1]);
        byte[] res = fileToByte(MConfig.prefix + "hg38.fa", begin, end);
        return  new ResponseEntity<>(res, headers, HttpStatus.PARTIAL_CONTENT);
    }
}
