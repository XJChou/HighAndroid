package com.zxj.plugin;

import com.android.build.api.transform.Transform;
import com.android.build.gradle.BaseExtension;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class ZJavaPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        /* extension注册 */
        ZBean bean = project.getExtensions().create("zxj", ZBean.class);
        project.afterEvaluate(project1 -> System.out.println("afterEvaluate value = " + bean.getName()));

        /* transform注册 */
        Transform transform =  new ZTransform();
        BaseExtension extension= project.getExtensions().getByType(BaseExtension.class);
        extension.registerTransform(transform);
    }
}
