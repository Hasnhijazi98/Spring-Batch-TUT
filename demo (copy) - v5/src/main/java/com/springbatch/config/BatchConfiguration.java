package com.springbatch.config;

import com.springbatch.Product;
import com.springbatch.processor.ProductProcessor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;


@Configuration
public class BatchConfiguration {

    private final DataSource dataSource;
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    public BatchConfiguration(DataSource dataSource, JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        this.dataSource = dataSource;
        this.jobRepository = jobRepository;
        this.transactionManager = transactionManager;
    }



    @Bean
    public ItemProcessor<Product,Product> myProcessor(){
        return new ProductProcessor();
    }


    @Bean
    public JdbcCursorItemReader<Product> jdbcItemReader() {
        return new JdbcCursorItemReaderBuilder<Product>()
                .name("productItemReader")
                .dataSource(dataSource)
                .sql("SELECT PRODUCT_ID, PRODUCT_NAME, PRODUCT_CATEGORY, PRODUCT_PRICE FROM PRODUCT_DETAILS")
                .rowMapper((rs, rowNum) -> {
                    Product product = new Product();
                    product.setProductId(rs.getString("PRODUCT_ID"));
                    product.setProductName(rs.getString("PRODUCT_NAME"));
                    product.setProductCategory(rs.getString("PRODUCT_CATEGORY"));
                    product.setProductPrice(rs.getDouble("PRODUCT_PRICE"));
                    return product;
                })
                .build();
    }

    @Bean
    public JdbcBatchItemWriter<Product> jdbcItemWriter() {
        return new JdbcBatchItemWriterBuilder<Product>()
                .itemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>())
                .sql("INSERT INTO WRITTEN_PRODUCTS (PRODUCT_ID, PRODUCT_NAME, PRODUCT_CATEGORY, PRODUCT_PRICE) VALUES (:productId, :productName, :productCategory, :productPrice)")
                .dataSource(dataSource)
                .build();
    }

    @Bean
    public ItemWriter<Product> DBitemWriter() {
        return items -> {
            System.out.println("Writing products taken from database...");
            for (Product product : items) {
                System.out.println(product.getProductName());
            }
        };
    }

    @Bean
    public FlatFileItemReader<Product> itemReader() {
        return new FlatFileItemReaderBuilder<Product>()
                .name("productItemReader")
                .resource(new ClassPathResource("Product_Details.csv"))  // CSV file location
                .linesToSkip(1) // Skip the header row
                .delimited()
                .names("product_id", "product_name", "product_category", "product_price")  // CSV column names
                .fieldSetMapper(new BeanWrapperFieldSetMapper<>() {{
                    setTargetType(Product.class);
                }})
                .build();
    }

    @Bean
    public FlatFileItemWriter<Product> CSVitemWriter() {
        return new FlatFileItemWriterBuilder<Product>()
                .name("productItemWriter")
                .resource(new FileSystemResource("src/main/resources/Output_Product_Details.csv")) // Output file location
                .delimited()
                .delimiter(",") // CSV delimiter
                .names("productId", "productName", "productCategory", "productPrice") // Field names to be written in the file
                .headerCallback(writer -> writer.write("Product ID,Product Name,Product Category,Product Price")) // Adding header
                .build();
    }

    @Bean
    public ItemWriter<Product> itemWriter() {
        return items -> {
            System.out.println("Writing products...");
            for (Product product : items) {
                System.out.println(product.getProductName());
            }
        };
    }

    /**
     * step pour ecrire et lire de données
     * @param jobRepository
     * @param transactionManager
     * @return
     */
//    @Bean
//    public Step step1(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
//        return new StepBuilder("chunkBasedStep1", jobRepository)
//                .<Product, Product>chunk(3,transactionManager)
//                .reader(itemReader())
//                .processor(myProcessor())
//               // .processor(itemProcessor())  // Optional, you can remove if you don't need processing
//                .writer(jdbcItemWriter())
//                .build();
//    }
//

    /**
     * steps pour les flow
     * @param jobRepository
     * @param transactionManger
     * @return
     */
    @Bean
    public Step step1(JobRepository jobRepository, PlatformTransactionManager transactionManger) {
        return new StepBuilder("step1", jobRepository).tasklet(new Tasklet() {

            @Override
            public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
                System.out.println("step1 executed!!");
                return RepeatStatus.FINISHED;
            }
        }, transactionManger).allowStartIfComplete(true).build();
    }

    @Bean
    public Step step2(JobRepository jobRepository, PlatformTransactionManager transactionManger) {
        return new StepBuilder("step2", jobRepository).tasklet(new Tasklet() {

            @Override
            public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
                System.out.println("step2 executed!!");
                return RepeatStatus.FINISHED;
            }
        }, transactionManger).allowStartIfComplete(true).build();
    }

    @Bean
    public Step step3(JobRepository jobRepository, PlatformTransactionManager transactionManger) {
        return new StepBuilder("step3", jobRepository).tasklet(new Tasklet() {

            @Override
            public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
                System.out.println("step3 executed!!");
                return RepeatStatus.FINISHED;
            }
        }, transactionManger).allowStartIfComplete(true).build();
    }

    @Bean
    public Step step4(JobRepository jobRepository, PlatformTransactionManager transactionManger) {
        return new StepBuilder("step4", jobRepository).tasklet(new Tasklet() {

            @Override
            public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
                System.out.println("step4 executed!! 3aw");
                return RepeatStatus.FINISHED;
            }
        }, transactionManger).allowStartIfComplete(true).build();
    }

    @Bean
    public Step jobStep(JobRepository jobRepository, Job secondJob) {
        return new StepBuilder("jobStep", jobRepository)
                .job(secondJob)
                .build();
    }

    /** job 1 step (pour tester des writers) **/
//    @Bean
//    public Job firstJob(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
//        return new JobBuilder( "job1", jobRepository)
//                .start(step1(jobRepository,transactionManager))
//                .build();
//    }

    @Bean
    public Flow myFlow(Step step2, Step step3) {
        return new FlowBuilder<Flow>("myFlow")
                .start(step2) // Replace `null` with your JobRepository and TransactionManager beans if needed
                .next(step3)
                .build();
    }

    @Bean
    public Flow myFlow2(Step step2, Step step3) {
        return new FlowBuilder<Flow>("myFlow2")
                .start(step2) // Replace `null` with your JobRepository and TransactionManager beans if needed
                .next(step3)
                .build();
    }

    @Bean
    public Job firstJob(JobRepository jobRepository, Flow myFlow, Flow myFlow2, Step jobStep) {
        return new JobBuilder( "job1", jobRepository)
                .start(myFlow)
                .split(new SimpleAsyncTaskExecutor()).add(myFlow2)
                .next(jobStep)
                .end()
                .build();
    }

    @Bean
    public Job secondJob(JobRepository jobRepository, Step step4, PlatformTransactionManager transactionManager) {
        return new JobBuilder("job2", jobRepository)
                .start(step4)
                .build();
    }
}