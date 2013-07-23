package org.jboss.wolf.validator.impl.version;

import java.io.File;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.maven.model.Model;
import org.jboss.wolf.validator.Validator;
import org.jboss.wolf.validator.ValidatorContext;
import org.jboss.wolf.validator.internal.ValidatorSupport;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

@Named
public class VersionAmbiguityValidator implements Validator {

    @Inject @Named("versionAmbiguityValidatorFilter")
    private IOFileFilter fileFilter;
    @Inject
    private ValidatorSupport validatorSupport;

    @Override
    public void validate(ValidatorContext ctx) {
        ListMultimap<String, File> ga2filesMap = ArrayListMultimap.create();
        ListMultimap<String, String> ga2versionsMap = ArrayListMultimap.create();

        List<Model> models = validatorSupport.resolveEffectiveModels(ctx, fileFilter);
        for (Model model : models) {
            String ga = model.getGroupId() + ":" + model.getArtifactId();
            ga2filesMap.put(ga, model.getPomFile());
            ga2versionsMap.put(ga, model.getVersion());
        }

        for (String ga : ga2versionsMap.keySet()) {
            List<String> versions = ga2versionsMap.get(ga);
            if (versions.size() > 1) {
                Exception e = new VersionAmbiguityException(ga, versions.toArray(new String[] {}));
                for (File pomFile : ga2filesMap.get(ga)) {
                    ctx.addException(pomFile, e);
                }
            }
        }
    }

}