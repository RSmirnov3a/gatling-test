package io.gatling.http.krista.request.builder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.gatling.core.session.Session;
import io.gatling.http.response.Response;
import scala.Option;

public class KClientHelper {

    public static final String KCLIENT_VAR = "KCLIENT_HELPER";
    private static ObjectMapper MAPPER = new ObjectMapper();

    private Session latestSession;

    private Map<String, ActiveForm> activeForms = new HashMap<>();

    public static KClientHelper resolve(Session session) {
        Option<Object> oclient = session.attributes().get(KCLIENT_VAR);
        if (oclient.isEmpty()) {
            KClientHelper client = new KClientHelper();
            session = session.set(KCLIENT_VAR, client);
            client.latestSession = session;
            return client;
        } else {
            KClientHelper client = (KClientHelper) oclient.get();
            client.latestSession = session;
            return client;
        }
    }

    public Session getLatestSessionState() {
        return latestSession;
    }

    private String readString(String name, String defaultValue) {
        return (String) latestSession.attributes().get(name).getOrElse(() -> defaultValue);
    }

    public ActiveForm form(String nameInScript) {
        return activeForms.computeIfAbsent(nameInScript, s -> new ActiveForm(s));
    }

    public class ActiveForm {

        final String nameInScript;

        String reportClass;
        String formId;
        String instanceId;
        String filterTreeId;
        String gridId;
        String panelName;

        BindingNode[] metadata;
        String masterKey;
        List<TabInfo> tabs = new ArrayList<>();
        DataAvailability[] dataAvailability;

        public ActiveForm(String nameInScript) {
            this.nameInScript = nameInScript;
        }

        public String getReportClass() {
            return this.reportClass;
        }

        public String getFormId() {
            return this.formId;
        }

        public String getInstanceId() {
            return this.instanceId;
        }
        public String getFilterTreeId() {
            return this.filterTreeId;
        }
        public String getGridId() {
            return this.gridId;
        }

        public String getPanelName() {
            return this.panelName;
        }

        public String getMasterGridBinding() {
            return String.valueOf(metadata[0].bindingLinkId);
        }

        public String getMasterKey() {
            return masterKey;
        }

        public void init(String reportClass, String formId) {
            this.reportClass = reportClass;
            this.formId = formId;
        }

        public void onFormOpen(Response response, String formInstanceCode) {
            this.instanceId = formInstanceCode;
        }

        public void loadMasterFormBindings(Response response) throws IOException {
            String body = response.body().string();
            metadata = MAPPER.readValue(body, BindingNode[].class);
            for (BindingNode node : metadata) {
                if ("Panel".equals(node.controlType)) {
                    gridId = String.valueOf(node.details.get(0).bindingLinkId);
                    panelName = String.valueOf(node.controlName);
                }
                if ("FilterTree".equals(node.controlType)) {
                    filterTreeId = String.valueOf(node.bindingLinkId);
                }
            }
        }

        public void onMasterGrid(Response response) throws IOException {
            String body = response.body().string();
            MasterGridInfo mgi = MAPPER.readValue(body, MasterGridInfo.class);
            for (int i = 0; i < mgi.fields.size(); ++i) {
                if ("id".equals(mgi.fields.get(i).get("name"))) {
                    masterKey = String.valueOf(mgi.plaindata.get(0).value.get(i));
                    break;
                }
            }
        }

        public void updateTabsDataAvailability(Response response) throws IOException {
            String body = response.body().string();
            dataAvailability = MAPPER.readValue(body, DataAvailability[].class);
        }

        public List<TabInfo> listTabBindingsWithData() {
            List<TabInfo> result = new ArrayList<>();
            for (int i = 0; i != dataAvailability[0].dataAvailable.length; ++i) {
                if (dataAvailability[0].dataAvailable[i] > 0) {
                    result.add(tabs.get(i));
                }
            }
            return result;
        }
    }

    public static class TabInfo {
        public String dataset;
        public String filter;
        public String grid;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class BindingNode {

        public long bindingLinkId;
        public String controlName;
        public String controlType;
        public List<BindingNode> details = new ArrayList<>();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class MasterGridInfo {
        public List<Map<String, String>> fields = new ArrayList<>();
        public List<Plaindata> plaindata = new ArrayList<>();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class Plaindata {
        public List<Object> value = new ArrayList<>();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class DataAvailability {
        public int[] dataAvailable = new int[0];
    }
}
