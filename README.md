# UPTODATE v0.3 (dev)

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file or the full text at [MIT License](https://opensource.org/licenses/MIT) for details.

## New amendments and features

1. The REST API has been enhanced by enforcing some of new API endpoints:
   - /api/articles/comments/get
   - /api/articles/comments/delete
   - /api/articles/comments/create
2. A storaging server has been applied to a project and bound with the Amazon S3 API

## Execution

**You are capable of executing the Backend by using Docker. Keep the further requirements:**
1. Download the project from the Github repository
2. In order to launch the project in the downloaded folder, you need to execute the further command: `docker-compose up --build`
4. The Docker environment is going to be assembled
5. After assembling, please, reboot all the containers
