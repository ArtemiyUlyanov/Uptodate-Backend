package me.artemiyulyanov.uptodate.services;

import jakarta.annotation.PostConstruct;
import me.artemiyulyanov.uptodate.models.ArticleTopic;
import me.artemiyulyanov.uptodate.repositories.ArticleTopicRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ArticleTopicService {
    public static final List<ArticleTopic> TOPICS = List.of(
            ArticleTopic.of("Technology", "Технологии", "Artificial Intelligence", "Искусственный интеллект"),
            ArticleTopic.of("Technology", "Технологии", "Software Development", "Разработка ПО"),
            ArticleTopic.of("Technology", "Технологии", "Cybersecurity", "Кибербезопасность"),
            ArticleTopic.of("Technology", "Технологии", "Blockchain", "Блокчейн"),
            ArticleTopic.of("Technology", "Технологии", "Cloud Computing", "Облачные вычисления"),
            ArticleTopic.of("Technology", "Технологии", "Virtual Reality & Augmented Reality", "Виртуальная реальность"),
            ArticleTopic.of("Technology", "Технологии", "Internet of Things (IoT)", "Интернет вещей"),
            ArticleTopic.of("Technology", "Технологии", "Big Data", "Большие данные"),
            ArticleTopic.of("Technology", "Технологии", "Wearable Tech", "Носимые технологии"),

            ArticleTopic.of("Health & Wellness", "Здоровье и благополучие", "Mental Health", "Ментальное здоровье"),
            ArticleTopic.of("Health & Wellness", "Здоровье и благополучие", "Nutrition", "Питание"),
            ArticleTopic.of("Health & Wellness", "Здоровье и благополучие", "Fitness & Exercise", "Фитнес и упражнения"),
            ArticleTopic.of("Health & Wellness", "Здоровье и благополучие", "Chronic Illness", "Хронические заболевания"),
            ArticleTopic.of("Health & Wellness", "Здоровье и благополучие", "Holistic Medicine", "Холистическая медицина"),
            ArticleTopic.of("Health & Wellness", "Здоровье и благополучие", "Medical Research", "Медицинские исследования"),
            ArticleTopic.of("Health & Wellness", "Здоровье и благополучие", "Sleep Health", "Здоровый сон"),
            ArticleTopic.of("Health & Wellness", "Здоровье и благополучие", "Skin Care", "Уход за кожей"),
            ArticleTopic.of("Health & Wellness", "Здоровье и благополучие", "Women's Health", "Женское здоровье"),
            ArticleTopic.of("Health & Wellness", "Здоровье и благополучие", "Physical Therapy", "Физиотерапия"),

            ArticleTopic.of("Business & Finance", "Бизнес и финансы", "Entrepreneurship", "Предпринимательство"),
            ArticleTopic.of("Business & Finance", "Бизнес и финансы", "Investing & Stock Market", "Инвестирование и фондовый рынок"),
            ArticleTopic.of("Business & Finance", "Бизнес и финансы", "Personal Finance", "Личные финансы"),
            ArticleTopic.of("Business & Finance", "Бизнес и финансы", "Cryptocurrency", "Криптовалюта"),
            ArticleTopic.of("Business & Finance", "Бизнес и финансы", "Corporate Strategies", "Корпоративные стратегии"),
            ArticleTopic.of("Business & Finance", "Бизнес и финансы", "Startups", "Стартапы"),
            ArticleTopic.of("Business & Finance", "Бизнес и финансы", "E-commerce", "Электронная коммерция"),
            ArticleTopic.of("Business & Finance", "Бизнес и финансы", "Real Estate", "Недвижимость"),
            ArticleTopic.of("Business & Finance", "Бизнес и финансы", "Marketing & Advertising", "Маркетинг и реклама"),
            ArticleTopic.of("Business & Finance", "Бизнес и финансы", "Leadership & Management", "Лидерство и управление"),

            ArticleTopic.of("Science & Innovation", "Наука и инновации", "Space Exploration", "Освоение космоса"),
            ArticleTopic.of("Science & Innovation", "Наука и инновации", "Environmental Science", "Науки об окружающей среде"),
            ArticleTopic.of("Science & Innovation", "Наука и инновации", "Physics", "Физика"),
            ArticleTopic.of("Science & Innovation", "Наука и инновации", "Chemistry", "Химия"),
            ArticleTopic.of("Science & Innovation", "Наука и инновации", "Biotechnology", "Биотехнологии"),
            ArticleTopic.of("Science & Innovation", "Наука и инновации", "Climate Change", "Изменение климата"),
            ArticleTopic.of("Science & Innovation", "Наука и инновации", "Renewable Energy", "Возобновляемая энергия"),
            ArticleTopic.of("Science & Innovation", "Наука и инновации", "Medical Breakthroughs", "Прорывы в медицине"),
            ArticleTopic.of("Science & Innovation", "Наука и инновации", "Quantum Computing", "Квантовые вычисления"),
            ArticleTopic.of("Science & Innovation", "Наука и инновации", "Robotics", "Робототехника"),

            ArticleTopic.of("Education & Learning", "Образование и учёба", "Online Learning", "Онлайн-обучение"),
            ArticleTopic.of("Education & Learning", "Образование и учёба", "STEM Education", "STEM-образование"),
            ArticleTopic.of("Education & Learning", "Образование и учёба", "Language Learning", "Изучение языков"),
            ArticleTopic.of("Education & Learning", "Образование и учёба", "Skill Development", "Развитие навыков"),
            ArticleTopic.of("Education & Learning", "Образование и учёба", "Educational Technology", "Образовательные технологии"),
            ArticleTopic.of("Education & Learning", "Образование и учёба", "Classroom Strategies", "Стратегии в классе"),
            ArticleTopic.of("Education & Learning", "Образование и учёба", "Higher Education", "Высшее образование"),
            ArticleTopic.of("Education & Learning", "Образование и учёба", "Early Childhood Education", "Дошкольное образование"),
            ArticleTopic.of("Education & Learning", "Образование и учёба", "Adult Learning", "Обучение для взрослых"),
            ArticleTopic.of("Education & Learning", "Образование и учёба", "Special Education", "Специальное образование"),

            ArticleTopic.of("Arts & Culture", "Искусство и культура", "Fine Arts (Painting, Sculpture)", "Изобразительное искусство (рисование, скульптинг)"),
            ArticleTopic.of("Arts & Culture", "Искусство и культура", "Music", "Музыка"),
            ArticleTopic.of("Arts & Culture", "Искусство и культура", "Literature", "Литература"),
            ArticleTopic.of("Arts & Culture", "Искусство и культура", "Film & Television", "Фильмы и телевидение"),
            ArticleTopic.of("Arts & Culture", "Искусство и культура", "Performing Arts (Theater, Dance)", "Исполнительное искусство (театр, танцы)"),
            ArticleTopic.of("Arts & Culture", "Искусство и культура", "Photography", "Фотография"),
            ArticleTopic.of("Arts & Culture", "Искусство и культура", "Cultural Heritage", "Культурное наследие"),
            ArticleTopic.of("Arts & Culture", "Искусство и культура", "Fashion", "Мода"),
            ArticleTopic.of("Arts & Culture", "Искусство и культура", "Architecture", "Архитектура"),
            ArticleTopic.of("Arts & Culture", "Искусство и культура", "Street Art", "Уличное искусство"),

            ArticleTopic.of("Travel & Adventure", "Путешествия и приключения", "Budget Travel", "Бюджетные путешествия"),
            ArticleTopic.of("Travel & Adventure", "Путешествия и приключения", "Luxury Travel", "Роскошные путешествия"),
            ArticleTopic.of("Travel & Adventure", "Путешествия и приключения", "Adventure Tourism", "Экстремальный туризм"),
            ArticleTopic.of("Travel & Adventure", "Путешествия и приключения", "Cultural Travel", "Культурные путешествия"),
            ArticleTopic.of("Travel & Adventure", "Путешествия и приключения", "Sustainable Travel", "Устойчивые путешествия"),
            ArticleTopic.of("Travel & Adventure", "Путешествия и приключения", "Solo Travel", "Путешествия в одиночку"),
            ArticleTopic.of("Travel & Adventure", "Путешествия и приключения", "Travel Tips & Hacks", "Советы и рекомендации для путешественников"),
            ArticleTopic.of("Travel & Adventure", "Путешествия и приключения", "Travel Photography", "Туристическая фотография"),
            ArticleTopic.of("Travel & Adventure", "Путешествия и приключения", "Backpacking", "Пеший туризм"),
            ArticleTopic.of("Travel & Adventure", "Путешествия и приключения", "City Guides", "Путеводители по городам"),

            ArticleTopic.of("Food & Drink", "Еда и напитки", "Recipes", "Рецепты"),
            ArticleTopic.of("Food & Drink", "Еда и напитки", "Baking", "Выпечка"),
            ArticleTopic.of("Food & Drink", "Еда и напитки", "Vegan & Vegetarian", "Веганство и вегетарианство"),
            ArticleTopic.of("Food & Drink", "Еда и напитки", "World Cuisines", "Блюда мировой кухни"),
            ArticleTopic.of("Food & Drink", "Еда и напитки", "Wine & Beer", "Вина и пиво"),
            ArticleTopic.of("Food & Drink", "Еда и напитки", "Street Food", "Уличная еда"),
            ArticleTopic.of("Food & Drink", "Еда и напитки", "Healthy Eating", "Здоровое питание"),
            ArticleTopic.of("Food & Drink", "Еда и напитки", "Food Science", "Наука о еде"),
            ArticleTopic.of("Food & Drink", "Еда и напитки", "Culinary Techniques", "Кулинарные техники"),
            ArticleTopic.of("Food & Drink", "Еда и напитки", "Restaurant Reviews", "Отзывы о ресторанах"),

            ArticleTopic.of("Lifestyle", "Образ жизни", "Home & Interior Design", "Дизайн дома и интерьера"),
            ArticleTopic.of("Lifestyle", "Образ жизни", "Fashion & Style", "Мода и стиль"),
            ArticleTopic.of("Lifestyle", "Образ жизни", "Self-Improvement", "Саморазвитие"),
            ArticleTopic.of("Lifestyle", "Образ жизни", "Relationships & Dating", "Отношения и свидания"),
            ArticleTopic.of("Lifestyle", "Образ жизни", "Work-Life Balance", "Баланс между работой и личной жизнью"),
            ArticleTopic.of("Lifestyle", "Образ жизни", "Sustainability in Lifestyle", "Устойчивость в образе жизни"),
            ArticleTopic.of("Lifestyle", "Образ жизни", "Minimalism", "Минимализм"),
            ArticleTopic.of("Lifestyle", "Образ жизни", "Parenting", "Воспитание детей"),
            ArticleTopic.of("Lifestyle", "Образ жизни", "Personal Growth", "Личностный рост"),
            ArticleTopic.of("Lifestyle", "Образ жизни", "Mindfulness", "Осознанность"),

            ArticleTopic.of("Sports & Recreation", "Спорт и отдых", "Football (Soccer)", "Футбол"),
            ArticleTopic.of("Sports & Recreation", "Спорт и отдых", "Basketball", "Баскетбол"),
            ArticleTopic.of("Sports & Recreation", "Спорт и отдых", "Tennis", "Теннис"),
            ArticleTopic.of("Sports & Recreation", "Спорт и отдых", "Extreme Sports", "Экстремальный спорт"),
            ArticleTopic.of("Sports & Recreation", "Спорт и отдых", "Olympics", "Олимпийские игры"),
            ArticleTopic.of("Sports & Recreation", "Спорт и отдых", "Martial Arts", "Боевые искусства"),
            ArticleTopic.of("Sports & Recreation", "Спорт и отдых", "Fitness & Bodybuilding", "Фитнес и бодибилдинг"),
            ArticleTopic.of("Sports & Recreation", "Спорт и отдых", "Water Sports", "Водные виды спорта"),
            ArticleTopic.of("Sports & Recreation", "Спорт и отдых", "Esports", "Киберспорт"),
            ArticleTopic.of("Sports & Recreation", "Спорт и отдых", "Running & Marathon", "Бег и марафоны")
    );

    @Autowired
    private ArticleTopicRepository articleTopicRepository;

    @PostConstruct
    public void init() {
        if (articleTopicRepository.count() > 0) return;

        List<ArticleTopic> topics = TOPICS;
        articleTopicRepository.saveAll(topics);
    }

    public List<ArticleTopic> findAll() {
        return articleTopicRepository.findAll();
    }

    public Optional<ArticleTopic> findById(Long id) {
        return articleTopicRepository.findById(id);
    }

    public List<ArticleTopic> findByParent(String parent) {
        return articleTopicRepository.findByParentInEnglishOrRussian(parent);
    }

    public Optional<ArticleTopic> findByName(String name) {
        return articleTopicRepository.findByNameInEnglishOrRussian(name);
    }
}