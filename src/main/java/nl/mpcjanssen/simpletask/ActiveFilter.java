package nl.mpcjanssen.simpletask;

import android.app.SearchManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import nl.mpcjanssen.simpletask.task.ByContextFilter;
import nl.mpcjanssen.simpletask.task.ByPriorityFilter;
import nl.mpcjanssen.simpletask.task.ByProjectFilter;
import nl.mpcjanssen.simpletask.task.ByTextFilter;
import nl.mpcjanssen.simpletask.task.Priority;
import nl.mpcjanssen.simpletask.task.Task;
import nl.mpcjanssen.simpletask.task.TaskFilter;
import nl.mpcjanssen.simpletask.util.Strings;
import nl.mpcjanssen.simpletask.util.Util;

/**
 * Active filter, has methods for serialisation in several formats
 */
public class ActiveFilter {
    static public final String NORMAL_SORT = "+";
    static public final String REVERSED_SORT = "-";
    static public final String SORT_SEPARATOR = "!";

    public final static String INTENT_TITLE = "TITLE";
    public final static String INTENT_SORT_ORDER = "SORTS";
    public final static String INTENT_CONTEXTS_FILTER = "CONTEXTS";
    public final static String INTENT_PROJECTS_FILTER = "PROJECTS";
    public final static String INTENT_PRIORITIES_FILTER = "PRIORITIES";
    public final static String INTENT_CONTEXTS_FILTER_NOT = "CONTEXTSnot";
    public final static String INTENT_PROJECTS_FILTER_NOT = "PROJECTSnot";
    public final static String INTENT_PRIORITIES_FILTER_NOT = "PRIORITIESnot";

    private  Resources mResources;

    private ArrayList<Priority> m_prios = new ArrayList<Priority>();
    private ArrayList<String> m_contexts = new ArrayList<String>();
    private ArrayList<String> m_projects = new ArrayList<String>();
    private ArrayList<String> m_sorts = new ArrayList<String>();
    private boolean m_projectsNot = false;
    private String m_search;
    private boolean m_priosNot;
    private boolean m_contextsNot;
    private String mName;

    public ActiveFilter(Resources resources) {
        this.mResources = resources;
    }


    public void initFromBundle(Bundle bundle) {
        m_prios = Priority.toPriority(bundle.getStringArrayList("m_prios"));
        m_contexts = bundle.getStringArrayList("m_contexts");
        m_projects = bundle.getStringArrayList("m_projects");
        m_search = bundle.getString("m_search");
        m_contextsNot = bundle.getBoolean("m_contextsNot");
        m_priosNot = bundle.getBoolean("m_priosNot");
        m_projectsNot = bundle.getBoolean("m_projectsNot");
        m_sorts = bundle.getStringArrayList("m_sorts");
    }

    public void initFromIntent(Intent intent) {
        String prios;
        String projects;
        String contexts;
        String sorts;

        prios = intent.getStringExtra(INTENT_PRIORITIES_FILTER);
        projects = intent.getStringExtra(INTENT_PROJECTS_FILTER);
        contexts = intent.getStringExtra(INTENT_CONTEXTS_FILTER);
        sorts = intent.getStringExtra(INTENT_SORT_ORDER);
        m_priosNot = intent.getBooleanExtra(
                INTENT_PRIORITIES_FILTER_NOT, false);
        m_projectsNot = intent.getBooleanExtra(
                INTENT_PROJECTS_FILTER_NOT, false);
        m_contextsNot = intent.getBooleanExtra(
                INTENT_CONTEXTS_FILTER_NOT, false);
        m_search = intent.getStringExtra(SearchManager.QUERY);
        if (sorts != null && !sorts.equals("")) {
            m_sorts = new ArrayList<String>(
                    Arrays.asList(sorts.split("\n")));
        }
        if (prios != null && !prios.equals("")) {
            m_prios = Priority.toPriority(Arrays.asList(prios.split("\n")));
        }
        if (projects != null && !projects.equals("")) {
            m_projects = new ArrayList<String>(Arrays.asList(projects
                    .split("\n")));
        }
        if (contexts != null && !contexts.equals("")) {
            m_contexts = new ArrayList<String>(Arrays.asList(contexts
                    .split("\n")));
        }
    }

    public void initFromPrefs(SharedPreferences prefs) {
        m_sorts = new ArrayList<String>();
        m_sorts.addAll(Arrays.asList(prefs.getString("m_sorts", "")
                .split("\n")));
        m_contexts = new ArrayList<String>(prefs.getStringSet(
                "m_contexts", Collections.<String>emptySet()));
        m_prios = Priority.toPriority(new ArrayList<String>(prefs
                .getStringSet("m_prios", Collections.<String>emptySet())));
        m_projects = new ArrayList<String>(prefs.getStringSet(
                "m_projects", Collections.<String>emptySet()));
        m_contextsNot = prefs.getBoolean("m_contextsNot", false);
        m_priosNot = prefs.getBoolean("m_priosNot", false);
        m_projectsNot = prefs.getBoolean("m_projectsNot", false);
        mName = prefs.getString(INTENT_TITLE, "Simpletask");
    }

    public boolean hasFilter() {
        return m_contexts.size() + m_projects.size() + m_prios.size() > 0
                || !Strings.isEmptyOrNull(m_search);
    }

    public String getTitle () {
        String filterTitle = mResources.getString(R.string.title_filter_applied);
        if (hasFilter()) {
            if (m_prios.size() > 0) {
                filterTitle += " " + mResources.getString(R.string.priority_prompt);
            }

            if (m_projects.size() > 0) {
                filterTitle += " " + mResources.getString(R.string.project_prompt);
            }

            if (m_contexts.size() > 0) {
                filterTitle += " " + mResources.getString(R.string.context_prompt);
            }
            if (m_search != null) {
                filterTitle += " " + mResources.getString(R.string.search);
            }
        } else {
                filterTitle = mResources.getString(R.string.no_filter);
        }
        return filterTitle;
    }

    public String getProposedName() {
        ArrayList<String> appliedFilters = new ArrayList<String>();
        appliedFilters.addAll(m_contexts);
        appliedFilters.addAll(Priority.inCode(m_prios));
        appliedFilters.addAll(m_projects);
        if (appliedFilters.size() == 1) {
            return appliedFilters.get(0);
        } else {
            return "";
        }
    }

    public ArrayList<String> getSort() {
        if (m_sorts == null || m_sorts.size() == 0
                || Strings.isEmptyOrNull(m_sorts.get(0))) {
            // Set a default sort
            m_sorts = new ArrayList<String>();
            for (String type : mResources.getStringArray(R.array.sortKeys)) {
                m_sorts.add(NORMAL_SORT + SORT_SEPARATOR
                        + type);
            }

        }
        return m_sorts;
    }

    public void saveInBundle(Bundle bundle) {
        bundle.putStringArrayList("m_prios", Priority.inCode(m_prios));
        bundle.putStringArrayList("m_contexts", m_contexts);
        bundle.putStringArrayList("m_projects", m_projects);
        bundle.putBoolean("m_contextsNot", m_contextsNot);
        bundle.putStringArrayList("m_sorts", m_sorts);
        bundle.putBoolean("m_priosNot", m_priosNot);
        bundle.putBoolean("m_projectsNot", m_projectsNot);
        bundle.putString("m_search", m_search);
    }

    public void saveInPrefs(SharedPreferences prefs) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(INTENT_TITLE, mName);
        editor.putString("m_sorts", Util.join(m_sorts, "\n"));
        editor.putStringSet("m_contexts", new HashSet<String>(m_contexts));
        editor.putStringSet("m_prios",
                new HashSet<String>(Priority.inCode(m_prios)));
        editor.putStringSet("m_projects", new HashSet<String>(m_projects));
        editor.putBoolean("m_contextsNot", m_contextsNot);
        editor.putBoolean("m_priosNot", m_priosNot);
        editor.putBoolean("m_projectsNot", m_projectsNot);
        editor.commit();
    }

    public void saveInIntent(Intent target) {
        target.putExtra(INTENT_CONTEXTS_FILTER, Util.join(m_contexts, "\n"));
        target.putExtra(INTENT_CONTEXTS_FILTER_NOT, m_contextsNot);
        target.putExtra(INTENT_PROJECTS_FILTER, Util.join(m_projects, "\n"));
        target.putExtra(INTENT_PROJECTS_FILTER_NOT, m_projectsNot);
        target.putExtra(INTENT_PRIORITIES_FILTER, Util.join(m_prios, "\n"));
        target.putExtra(INTENT_PRIORITIES_FILTER_NOT, m_priosNot);
        target.putExtra(INTENT_SORT_ORDER, Util.join(m_sorts, "\n"));
        target.putExtra(SearchManager.QUERY, m_search);
    }


    public void clear() {
        m_prios = new ArrayList<Priority>();
        m_contexts = new ArrayList<String>();
        m_projects = new ArrayList<String>();
        m_projectsNot = false;
        m_search = null;
        m_priosNot = false;
        m_contextsNot = false;
    }

    public void setSearch(String search) {
        this.m_search = search;
    }

    public ArrayList<String> getContexts() {
        return m_contexts;
    }

    public boolean getContextsNot() {
        return m_contextsNot;
    }

    public void setContextsNot(boolean state) {
        this.m_contextsNot = state;
    }

    public ArrayList<String> getProjects() {
        return m_projects;
    }

    public boolean getProjectsNot() {
        return m_projectsNot;
    }

    public ArrayList<Priority> getPriorities() {
        return m_prios;
    }

    public void setProjectsNot(boolean state) {
        this.m_projectsNot = state;
    }

    public void setContexts(ArrayList<String> contexts) {
        this.m_contexts = contexts;
    }

    public void setProjects(ArrayList<String> projects) {
        this.m_projects = projects;
    }

    public ArrayList<Task> apply(ArrayList<Task> tasks, boolean showCompleted) {
        AndFilter filter = new AndFilter();
        ArrayList<Task> matched = new ArrayList<Task>();
        for (Task t : tasks) {
            if (t.isCompleted() && !showCompleted) {
                continue;
            }
            if (filter.apply(t)) {
                matched.add(t);
            }
        }
        return matched;
    }

    public boolean getPrioritiesNot() {
        return m_priosNot;
    }

    public void setPriorities(ArrayList<String> prios) {
        m_prios = Priority.toPriority(prios);
    }

    public void setPrioritiesNot(boolean prioritiesNot) {
        this.m_priosNot = prioritiesNot;
    }

    public void setSort(ArrayList<String> sort) {
        this.m_sorts = sort;
    }

    public void setName(String name) {
        this.mName = name;
    }

    public String getName() {
        return mName;
    }


    private class AndFilter {
        private ArrayList<TaskFilter> filters = new ArrayList<TaskFilter>();

        private AndFilter() {
            filters.clear();
            if (m_prios.size() > 0) {
                addFilter(new ByPriorityFilter(m_prios, m_priosNot));
            }
            if (m_contexts.size() > 0) {
                addFilter(new ByContextFilter(m_contexts, m_contextsNot));
            }
            if (m_projects.size() > 0) {
                addFilter(new ByProjectFilter(m_projects, m_projectsNot));
            }

            if (!Strings.isEmptyOrNull(m_search)) {
                addFilter(new ByTextFilter(m_search, false));
            }
        }

        public void addFilter(TaskFilter filter) {
            if (filter != null) {
                filters.add(filter);
            }
        }

        public boolean apply(Task input) {
            for (TaskFilter f : filters) {
                if (!f.apply(input)) {
                    return false;
                }
            }
            return true;
        }
    }
}
