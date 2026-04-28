package stud.g33;

import core.game.Game;
import core.game.ui.Configuration;
import java.util.*;

/**
 * 六子棋评估函数优化器 - 遗传算法 (Genetic Algorithm) 实现
 * 核心逻辑：种群进化、锦标赛适应度评估、均匀交叉与自适应变异
 */
public class GATuner {
    // 算法超参数设置
    static final int POPULATION_SIZE = 8;  // 种群大小
    static final int GENERATIONS = 10;     // 进化代数
    static final double MUTATION_RATE = 0.15; // 变异概率

    // 初始基因基准（参考论文中的经验值）
    static final int[] BASE_GENE = {0, 17, 78, 141, 788, 1030, 10000000};

    /**
     * 个体类：代表一个拥有特定评估参数的 AI
     */
    static class Individual {
        int[] chromosome = new int[7]; // 基因型：即 ScoreOfRoad[0-6]
        double fitness = 1.0;          // 适应度初值
        String id;

        // 初始种群生成：在基准值基础上浮动 ±20%
        public Individual(String id) {
            this.id = id;
            this.chromosome[0] = 0;
            this.chromosome[6] = 10000000;
            for (int i = 1; i <= 5; i++) {
                double fluctuation = 0.8 + (Math.random() * 0.4);
                this.chromosome[i] = (int) (BASE_GENE[i] * fluctuation);
            }
        }

        public Individual(int[] gene, String id) {
            this.chromosome = gene.clone();
            this.id = id;
        }
    }

    public static void main(String[] args) throws InterruptedException {
        // 【关键】强制关闭 GUI 以实现极速对战训练
        Configuration.GUI = false;

        List<Individual> population = new ArrayList<>();
        for (int i = 0; i < POPULATION_SIZE; i++) {
            population.add(new Individual("Gen0_Bot_" + i));
        }

        System.out.println(">>> 遗传算法离线进化开始...");

        for (int gen = 1; gen <= GENERATIONS; gen++) {
            System.out.println("\n--- 第 " + gen + " 代进化中 ---");

            // 1. 适应度评估：采用锦标赛制
            evaluatePopulationFitness(population);

            // 2. 排序并展示本代最强基因
            population.sort((a, b) -> Double.compare(b.fitness, a.fitness));
            Individual elite = population.get(0);
            System.out.println("本代冠军基因: " + Arrays.toString(elite.chromosome) + " | 适应度: " + elite.fitness);

            // 3. 产生下一代 (选择、交叉、变异)
            if (gen < GENERATIONS) {
                population = produceNextGeneration(population, gen);
            }
        }
        System.out.println("\n>>> 进化实验结束。请将最优基因拷贝至 Connect6Engine 的构造函数中。");
    }

    /**
     * 适应度评估逻辑：分组循环赛
     */
    private static void evaluatePopulationFitness(List<Individual> pop) throws InterruptedException {
        // 重置得分
        for (Individual ind : pop) ind.fitness = 0;

        // 模拟小组赛：两两对弈，胜得2分，平得1分，负得0分
        for (int i = 0; i < pop.size(); i++) {
            for (int j = i + 1; j < pop.size(); j++) {
                Individual p1 = pop.get(i);
                Individual p2 = pop.get(j);

                // 注入基因实例化 AI
                Connect6Engine bot1 = new Connect6Engine(p1.chromosome, p1.id);
                Connect6Engine bot2 = new Connect6Engine(p2.chromosome, p2.id);

                // 进行双循环赛（交换先后手）以示公平
                runMatch(bot1, bot2);
                runMatch(bot2, bot1);

                // 累加适应度分数
                p1.fitness += bot1.scores();
                p2.fitness += bot2.scores();
            }
        }
    }

    private static void runMatch(Connect6Engine b1, Connect6Engine b2) throws InterruptedException {
        Game game = new Game(b1, b2); // 使用框架自带的对战逻辑
        game.start();
        while (game.running()) {
            Thread.sleep(1); // 极短阻塞等待
        }
    }

    /**
     * 进化算子：精英保留 + 均匀交叉 + 抖动变异
     */
    private static List<Individual> produceNextGeneration(List<Individual> parents, int gen) {
        List<Individual> nextGen = new ArrayList<>();
        Random rand = new Random();

        // 1. 精英保留策略：直接保留本代表现最好的 2 个个体
        nextGen.add(new Individual(parents.get(0).chromosome, "Elite_0_Gen" + gen));
        nextGen.add(new Individual(parents.get(1).chromosome, "Elite_1_Gen" + gen));

        // 2. 生成后代
        while (nextGen.size() < POPULATION_SIZE) {
            // 选择父代（从前50%中随机选）
            Individual father = parents.get(rand.nextInt(POPULATION_SIZE / 2));
            Individual mother = parents.get(rand.nextInt(POPULATION_SIZE / 2));

            int[] childGene = new int[7];
            childGene[0] = 0; childGene[6] = 10000000;

            // 均匀交叉
            for (int k = 1; k <= 5; k++) {
                childGene[k] = rand.nextBoolean() ? father.chromosome[k] : mother.chromosome[k];

                // 自适应变异：产生 [-0.15, 0.15] 的随机抖动
                if (rand.nextDouble() < MUTATION_RATE) {
                    double rate = -0.15 + (rand.nextDouble() * 0.3);
                    childGene[k] = (int) (childGene[k] * (1 + rate));
                }
            }
            nextGen.add(new Individual(childGene, "Bot_" + nextGen.size() + "_Gen" + gen));
        }
        return nextGen;
    }
}