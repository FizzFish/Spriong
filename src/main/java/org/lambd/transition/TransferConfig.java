package org.lambd.transition;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.File;
import java.util.List;

public class TransferConfig {
    private String configFile;
    public TransferConfig(String configFile) {
        this.configFile = configFile;
    }
    public List<BaseTransfer> parse() {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        try {
            List<BaseTransfer> transfers = mapper.readValue(new File(configFile), new TypeReference<List<BaseTransfer>>() {});
            // 输出结果，验证是否正确解析
            return transfers;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return List.of();
    }
    public static void main(String[] args) {
        new TransferConfig("src/main/resources/transfer.yml").parse();
    }
}
