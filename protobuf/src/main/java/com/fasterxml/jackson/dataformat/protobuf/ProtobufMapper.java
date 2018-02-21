package com.fasterxml.jackson.dataformat.protobuf;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import com.fasterxml.jackson.core.Version;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.cfg.MapperBuilder;

import com.fasterxml.jackson.dataformat.protobuf.schema.DescriptorLoader;
import com.fasterxml.jackson.dataformat.protobuf.schema.FileDescriptorSet;
import com.fasterxml.jackson.dataformat.protobuf.schema.ProtobufSchema;
import com.fasterxml.jackson.dataformat.protobuf.schema.ProtobufSchemaLoader;
import com.fasterxml.jackson.dataformat.protobuf.schemagen.ProtobufSchemaGenerator;

public class ProtobufMapper extends ObjectMapper
{
    private static final long serialVersionUID = 1L;

    /**
     * Base implementation for "Vanilla" {@link ObjectMapper}, used with
     * Avro backend.
     *
     * @since 3.0
     */
    public static class Builder extends MapperBuilder<ProtobufMapper, Builder>
    {
        public Builder(ProtobufFactory f) {
            super(f);
        }

        @Override
        public ProtobufMapper build() {
            return new ProtobufMapper(this);
        }
    }

    protected ProtobufSchemaLoader _schemaLoader = ProtobufSchemaLoader.std;

    /**
     * Lazily constructed instance of {@link DescriptorLoader}, used for loading
     * structured protoc definitions from multiple files.
     */
    protected DescriptorLoader _descriptorLoader;

    /*
    /**********************************************************************
    /* Life-cycle
    /**********************************************************************
     */

    public ProtobufMapper() {
        this(new ProtobufFactory());
    }

    public ProtobufMapper(ProtobufFactory f) {
        this(new Builder(f));
    }

    public ProtobufMapper(Builder b) {
        super(b);
    }

    @SuppressWarnings("unchecked")
    public static Builder builder() {
        return new Builder(new ProtobufFactory());
    }

    public static Builder builder(ProtobufFactory streamFactory) {
        return new Builder(streamFactory);
    }

    /*
    /**********************************************************************
    /* Basic accessor overrides
    /**********************************************************************
     */
    
    @Override
    public Version version() {
        return PackageVersion.VERSION;
    }

    @Override
    public ProtobufFactory tokenStreamFactory() {
        return (ProtobufFactory) _streamFactory;
    }

    /*
    /**********************************************************************
    /* Schema access, single protoc source
    /**********************************************************************
     */

    /**
     * Accessor for reusable {@link ProtobufSchemaLoader} which can be
     * used for loading protoc definitions from files and other external
     * sources.
     */
    public ProtobufSchemaLoader schemaLoader() {
        return _schemaLoader;
    }

    public void setSchemaLoader(ProtobufSchemaLoader l) {
        _schemaLoader = l;
    }

    /**
     * Convenience method for constructing protoc definition that matches
     * given Java type. Uses {@link ProtobufSchemaGenerator} for
     * generation.
     */
    public ProtobufSchema generateSchemaFor(JavaType type) throws JsonMappingException
    {
        ProtobufSchemaGenerator gen = new ProtobufSchemaGenerator();
        acceptJsonFormatVisitor(type, gen);
        return gen.getGeneratedSchema();
    }

    /**
     * Convenience method for constructing protoc definition that matches
     * given Java type. Uses {@link ProtobufSchemaGenerator} for
     * generation.
     */
    public ProtobufSchema generateSchemaFor(Class<?> type) throws JsonMappingException
    {
        ProtobufSchemaGenerator gen = new ProtobufSchemaGenerator();
        acceptJsonFormatVisitor(type, gen);
        return gen.getGeneratedSchema();
    }

    /*
    /**********************************************************************
    /* Schema access, FileDescriptorSets (since 2.9)
    /**********************************************************************
     */

    public FileDescriptorSet loadDescriptorSet(URL src) throws IOException {
        return descriptorLoader().load(src);
    }

    public FileDescriptorSet loadDescriptorSet(File src) throws IOException {
        return descriptorLoader().load(src);
    }

    public FileDescriptorSet loadDescriptorSet(InputStream src) throws IOException {
        return descriptorLoader().load(src);
    }

    /**
     * Accessors that may be used instead of convenience <code>loadDescriptorSet</code>
     * methods, if alternate sources need to be used.
     */
    public synchronized DescriptorLoader descriptorLoader() throws IOException
    {
        DescriptorLoader l = _descriptorLoader;
        if (l == null) {
            _descriptorLoader = l = DescriptorLoader.construct(this);
        }
        return l;
    }
}
