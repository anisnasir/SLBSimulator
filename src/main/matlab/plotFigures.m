sep = '_';
basefilename = 'output';
datafilename = 'twitter';
%datafilename = 'wiki';
datadir = strcat(datafilename, sep, basefilename);
datadir = strcat('../../../data/', datadir);
%method = 'ch';
method = 'potc5';

granularity = { 'MINUTE', 'TENMIN', 'TWENTYMIN', 'HALFHOUR', 'HOUR' };
nsstr = {'5' '10' '50' '100' };
nrstr = {'5' '10' '100' '1000' '100000' };

PWD = pwd;
NUM_FIELDS = 3;
cd(datadir);

for ns = 1:length(nsstr)
    for nr = 1:length(nrstr)
        for g = 1:length(granularity)
            suffix = strcat(sep, datafilename, sep, nsstr(ns), sep, nrstr(nr), sep, method, sep, granularity(g));
            filename = strcat(basefilename, suffix, '.txt');
            data = load(char(filename));
            msg = data(:,1:NUM_FIELDS:end);
            dict = data(:,2:NUM_FIELDS:end);
            
            figure;
            ph = plot(msg,'-');
            set(ph, 'Linewidth', 2);
            set(ph, 'MarkerSize', 1);
            %lh = legend('first','second','third');
            %set(lh, 'Fontsize',24,'Location','SouthEast');
            xlabel('Time','Fontsize',20);
            ylabel('Number of messages','Fontsize',20);
            title(strcat(datafilename,', servers=',nsstr(ns),', replicas=',nrstr(nr)),'Fontsize',22);
            set(gca, 'Fontsize',20);
            %set(gca, 'XLim', [ xmin xmax ], 'YLim', [ ymin ymax ], 'Fontsize',24);
            print('-depsc2', char(strcat('figure', suffix,'.eps')));
            close
        end
    end
end

cd(PWD);