package com.web.coretix.constants;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.IntFunction;

public final class RoleModuleCatalog {

    private static final Map<CoreAppModule, ModuleDefinition> DEFINITIONS = buildDefinitions();

    private RoleModuleCatalog() {
    }

    public static List<CoreAppModule> getRoleModules() {
        return new ArrayList<>(DEFINITIONS.keySet());
    }

    public static List<String> getSubmoduleValues(CoreAppModule module) {
        ModuleDefinition definition = DEFINITIONS.get(module);
        return definition == null ? new ArrayList<>() : definition.submoduleValuesProvider.get();
    }

    public static List<Integer> getSubmoduleIds(CoreAppModule module) {
        ModuleDefinition definition = DEFINITIONS.get(module);
        return definition == null ? new ArrayList<>() : definition.submoduleIdsProvider.get();
    }

    public static String resolveSubmoduleName(int moduleId, int submoduleId) {
        CoreAppModule module = CoreAppModule.getById(moduleId);
        ModuleDefinition definition = DEFINITIONS.get(module);
        if (definition == null) {
            throw new IllegalArgumentException("Unsupported module id: " + moduleId);
        }
        return definition.submoduleNameResolver.apply(submoduleId);
    }

    public static int resolveSubmoduleId(String moduleName, String submoduleName) {
        CoreAppModule module = CoreAppModule.getByValue(moduleName);
        ModuleDefinition definition = DEFINITIONS.get(module);
        if (definition == null) {
            throw new IllegalArgumentException("Unsupported module name: " + moduleName);
        }
        return definition.submoduleIdResolver.apply(submoduleName);
    }

    private static Map<CoreAppModule, ModuleDefinition> buildDefinitions() {
        Map<CoreAppModule, ModuleDefinition> definitions = new EnumMap<>(CoreAppModule.class);
        definitions.put(CoreAppModule.USER_MANAGEMENT, new ModuleDefinition(
                UserManagementModule::getAllValues,
                () -> enumIds(UserManagementModule.values()),
                id -> UserManagementModule.getById(id).getValue(),
                value -> UserManagementModule.getByValue(value).getId()));
        definitions.put(CoreAppModule.SYSTEM_MANAGEMENT, new ModuleDefinition(
                SystemManagementModule::getAllValues,
                () -> enumIds(SystemManagementModule.values()),
                id -> SystemManagementModule.getById(id).getValue(),
                value -> SystemManagementModule.getByValue(value).getId()));
        definitions.put(CoreAppModule.LICENCE, new ModuleDefinition(
                LicenseManagementModule::getAllValues,
                () -> enumIds(LicenseManagementModule.values()),
                id -> LicenseManagementModule.getById(id).getValue(),
                value -> LicenseManagementModule.getByValue(value).getId()));
        definitions.put(CoreAppModule.SERVER_AND_DB, new ModuleDefinition(
                ServerAndDBModule::getAllValues,
                () -> enumIds(ServerAndDBModule.values()),
                id -> ServerAndDBModule.getById(id).getValue(),
                value -> ServerAndDBModule.getByValue(value).getId()));
        definitions.put(CoreAppModule.APPLICATION_MANAGEMENT, new ModuleDefinition(
                ApplicationManagementModule::getAllValues,
                () -> enumIds(ApplicationManagementModule.values()),
                id -> ApplicationManagementModule.getById(id).getValue(),
                value -> ApplicationManagementModule.getByValue(value).getId()));
        definitions.put(CoreAppModule.CAREX, new ModuleDefinition(
                CarexRolePageModule::getAllValues,
                () -> enumIds(CarexRolePageModule.values()),
                id -> CarexRolePageModule.getById(id).getValue(),
                value -> CarexRolePageModule.getByValue(value).getId()));
        return definitions;
    }

    private static <T extends Enum<T>> List<Integer> enumIds(T[] values) {
        List<Integer> ids = new ArrayList<>();
        for (T value : values) {
            try {
                ids.add((Integer) value.getClass().getMethod("getId").invoke(value));
            } catch (Exception exception) {
                throw new IllegalStateException("Unable to resolve enum id for " + value, exception);
            }
        }
        return ids;
    }

    private static final class ModuleDefinition {
        private final ListSupplier submoduleValuesProvider;
        private final IntegerListSupplier submoduleIdsProvider;
        private final IntFunction<String> submoduleNameResolver;
        private final Function<String, Integer> submoduleIdResolver;

        private ModuleDefinition(ListSupplier submoduleValuesProvider,
                                 IntegerListSupplier submoduleIdsProvider,
                                 IntFunction<String> submoduleNameResolver,
                                 Function<String, Integer> submoduleIdResolver) {
            this.submoduleValuesProvider = submoduleValuesProvider;
            this.submoduleIdsProvider = submoduleIdsProvider;
            this.submoduleNameResolver = submoduleNameResolver;
            this.submoduleIdResolver = submoduleIdResolver;
        }
    }

    @FunctionalInterface
    private interface ListSupplier {
        List<String> get();
    }

    @FunctionalInterface
    private interface IntegerListSupplier {
        List<Integer> get();
    }
}
