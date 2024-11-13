package unical.enterpriceapplication.onlycards.application.config.filter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class CustomRequestWrapper extends HttpServletRequestWrapper {
        private final String modifiedRequestURI;
    private final Map<String, String[]> modifiedParameters;

    public CustomRequestWrapper(HttpServletRequest request) {
        super(request);
        this.modifiedRequestURI = request.getRequestURI().replaceAll("%20", " ");
        this.modifiedParameters = modifyParameters(request.getParameterMap());
        log.info("Request URI: {}", this.modifiedRequestURI);
        this.modifiedParameters.forEach((key, value) -> log.info("Request parameter: {} = {}", key, value));

    }

    @Override
        public String getRequestURI() {
            return this.modifiedRequestURI;
        }

    @Override
    public String getParameter(String name) {
        String[] values = modifiedParameters.get(name);
        return values != null && values.length > 0 ? values[0] : null;
    }


@Override
public Map<String, String[]> getParameterMap() {
    return modifiedParameters;
}

@Override
public String[] getParameterValues(String name) {
    return modifiedParameters.get(name);
}

private Map<String, String[]> modifyParameters(Map<String, String[]> originalParameters) {
    Map<String, String[]> modifiedParams = new HashMap<>();
    for (Map.Entry<String, String[]> entry : originalParameters.entrySet()) {
        String key = entry.getKey().replaceAll("%20", " ");
        String[] values = entry.getValue();
        String[] modifiedValues = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            modifiedValues[i] = values[i].replaceAll("%20", " ");
        }
        modifiedParams.put(key, modifiedValues);
    }
    return modifiedParams;
}}

